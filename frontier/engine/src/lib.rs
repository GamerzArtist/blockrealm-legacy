//! Deterministic, platform-independent voxel simulation primitives.
//! The existing playable clients have not yet been wired to this library.
use std::collections::BTreeMap;

pub const TICK_HZ: u32 = 20;
pub const CHUNK: i32 = 16;
pub const HEIGHT: i32 = 384;
pub const LIMIT: i32 = 30_000_000;
pub const AIR: u16 = 0;
pub const BEDROCK: u16 = 1;
pub const STONE: u16 = 2;
pub const SOIL: u16 = 3;
pub const GRASS: u16 = 4;
pub const SAND: u16 = 5;
pub const WATER: u16 = 6;
pub const SNOW: u16 = 7;
const MAX_EDITS: usize = 1_000_000;

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Pos {
    pub x: i32,
    pub y: i32,
    pub z: i32,
}

impl Pos {
    pub fn valid(self) -> bool {
        (-LIMIT..=LIMIT).contains(&self.x)
            && (-LIMIT..=LIMIT).contains(&self.z)
            && (0..HEIGHT).contains(&self.y)
    }
    pub fn address(self) -> Option<(i32, i32, usize)> {
        if !self.valid() { return None; }
        let x = self.x.rem_euclid(CHUNK) as usize;
        let z = self.z.rem_euclid(CHUNK) as usize;
        Some((self.x.div_euclid(CHUNK), self.z.div_euclid(CHUNK),
            self.y as usize * 256 + z * 16 + x))
    }
}

fn mix(mut v: u64) -> u64 {
    v = (v ^ (v >> 30)).wrapping_mul(0xbf58476d1ce4e5b9);
    v = (v ^ (v >> 27)).wrapping_mul(0x94d049bb133111eb);
    v ^ (v >> 31)
}

fn lattice(seed: u64, x: i32, z: i32) -> i64 {
    let h = mix(seed ^ (x as i64 as u64).wrapping_mul(0x9e3779b97f4a7c15)
        ^ (z as i64 as u64).wrapping_mul(0xd1b54a32d192ed03));
    (h >> 48) as i64 - 32768
}

/// Integer-only, smoothed value noise: identical arithmetic on native and Wasm.
fn noise(seed: u64, x: i32, z: i32, scale: i32) -> i64 {
    let ix = x.div_euclid(scale);
    let iz = z.div_euclid(scale);
    let smooth = |n: i32| {
        let t = n as i64 * 1024 / scale as i64;
        t * t * (3072 - 2 * t) / (1024 * 1024)
    };
    let tx = smooth(x.rem_euclid(scale));
    let tz = smooth(z.rem_euclid(scale));
    let lerp = |a: i64, b: i64, t: i64| a + (b - a) * t / 1024;
    lerp(lerp(lattice(seed, ix, iz), lattice(seed, ix + 1, iz), tx),
         lerp(lattice(seed, ix, iz + 1), lattice(seed, ix + 1, iz + 1), tx), tz)
}

pub fn height(seed: u64, x: i32, z: i32) -> i32 {
    let a = noise(seed, x, z, 128) * 28 / 32768;
    let b = noise(seed ^ 1, x, z, 32) * 10 / 32768;
    let c = noise(seed ^ 2, x, z, 8) * 3 / 32768;
    (66 + a + b + c).clamp(8, 180) as i32
}

pub fn generated(seed: u64, p: Pos) -> u16 {
    if !p.valid() { return AIR; }
    if p.y == 0 { return BEDROCK; }
    let h = height(seed, p.x, p.z);
    if p.y > h { return if p.y <= 62 { WATER } else { AIR }; }
    let dry = noise(seed ^ 3, p.x, p.z, 192) > 16000;
    let cold = noise(seed ^ 4, p.x, p.z, 256) < -16000;
    if p.y == h {
        if h < 65 || dry { SAND } else if cold { SNOW } else { GRASS }
    } else if p.y >= h - 3 {
        if h < 65 || dry { SAND } else { SOIL }
    } else { STONE }
}

fn known(block: u16) -> bool { block <= SNOW }
fn solid(block: u16) -> bool { block != AIR && block != WATER }

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct World {
    pub seed: u64,
    edits: BTreeMap<Pos, u16>,
}

impl World {
    pub fn new(seed: u64) -> Self { Self { seed, edits: BTreeMap::new() } }
    pub fn block(&self, p: Pos) -> u16 {
        self.edits.get(&p).copied().unwrap_or_else(|| generated(self.seed, p))
    }
    fn set(&mut self, p: Pos, block: u16) -> Result<(), Reject> {
        if !p.valid() || p.y == 0 || !known(block) || block == BEDROCK {
            return Err(Reject::InvalidBlock);
        }
        if generated(self.seed, p) == block {
            self.edits.remove(&p);
        } else {
            if self.edits.len() >= MAX_EDITS && !self.edits.contains_key(&p) {
                return Err(Reject::Capacity);
            }
            self.edits.insert(p, block);
        }
        Ok(())
    }

    pub fn chunk(&self, cx: i32, cz: i32) -> Option<Vec<u16>> {
        let x0 = cx.checked_mul(CHUNK)?;
        let z0 = cz.checked_mul(CHUNK)?;
        if !(-LIMIT..=LIMIT-15).contains(&x0) || !(-LIMIT..=LIMIT-15).contains(&z0) {
            return None;
        }
        let mut cells = vec![AIR; (HEIGHT * CHUNK * CHUNK) as usize];
        // Generate each column only once; overlay sparse, authoritative edits below.
        for z in 0..16 {
            for x in 0..16 {
                let wx = x0 + x;
                let wz = z0 + z;
                let h = height(self.seed, wx, wz);
                let dry = noise(self.seed ^ 3, wx, wz, 192) > 16000;
                let cold = noise(self.seed ^ 4, wx, wz, 256) < -16000;
                for y in 0..HEIGHT {
                    let b = if y == 0 { BEDROCK }
                    else if y > h { if y <= 62 { WATER } else { AIR } }
                    else if y == h { if h < 65 || dry { SAND } else if cold { SNOW } else { GRASS } }
                    else if y >= h - 3 { if h < 65 || dry { SAND } else { SOIL } }
                    else { STONE };
                    cells[(y * 256 + z * 16 + x) as usize] = b;
                }
            }
        }
        for (&p, &block) in &self.edits {
            if let Some((a,b,i)) = p.address() {
                if a == cx && b == cz { cells[i] = block; }
            }
        }
        Some(cells)
    }

    /// Canonical little-endian world-edit snapshot. Player state is separate.
    pub fn encode(&self) -> Vec<u8> {
        let mut out = Vec::with_capacity(16 + self.edits.len() * 14);
        out.extend_from_slice(b"BRW1");
        out.extend_from_slice(&self.seed.to_le_bytes());
        out.extend_from_slice(&(self.edits.len() as u32).to_le_bytes());
        for (&p, &b) in &self.edits {
            out.extend_from_slice(&p.x.to_le_bytes());
            out.extend_from_slice(&p.y.to_le_bytes());
            out.extend_from_slice(&p.z.to_le_bytes());
            out.extend_from_slice(&b.to_le_bytes());
        }
        out
    }

    pub fn decode(bytes: &[u8]) -> Result<Self, Reject> {
        if bytes.len() < 16 || &bytes[..4] != b"BRW1" { return Err(Reject::BadSnapshot); }
        let seed = u64::from_le_bytes(bytes[4..12].try_into().unwrap());
        let count = u32::from_le_bytes(bytes[12..16].try_into().unwrap()) as usize;
        if count > MAX_EDITS || bytes.len() != 16 + count * 14 {
            return Err(Reject::BadSnapshot);
        }
        let mut world = Self::new(seed);
        let mut previous = None;
        for row in bytes[16..].chunks_exact(14) {
            let p = Pos {
                x: i32::from_le_bytes(row[0..4].try_into().unwrap()),
                y: i32::from_le_bytes(row[4..8].try_into().unwrap()),
                z: i32::from_le_bytes(row[8..12].try_into().unwrap()),
            };
            let b = u16::from_le_bytes(row[12..14].try_into().unwrap());
            if previous.is_some_and(|v| v >= p) || !p.valid() ||
                generated(seed, p) == b || world.set(p,b).is_err() {
                return Err(Reject::BadSnapshot);
            }
            previous = Some(p);
        }
        Ok(world)
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Mode { Survival, Creative, Hardcore, Spectator }

#[derive(Debug, Clone)]
pub struct Player {
    pub mode: Mode,
    pub position_mm: [i64; 3],
    inventory: BTreeMap<u16, u32>,
    last_sequence: u64,
}

impl Player {
    pub fn new(mode: Mode, position_mm: [i64; 3]) -> Self {
        Self { mode, position_mm, inventory: BTreeMap::new(), last_sequence: 0 }
    }
    pub fn count(&self, block: u16) -> u32 {
        self.inventory.get(&block).copied().unwrap_or(0)
    }
    pub fn grant(&mut self, block: u16, count: u32) -> Result<(), Reject> {
        if !solid(block) || block == BEDROCK || !known(block) { return Err(Reject::InvalidBlock); }
        let n = self.count(block).checked_add(count).ok_or(Reject::Capacity)?;
        self.inventory.insert(block,n);
        Ok(())
    }
}

#[derive(Debug, Clone, Copy)]
pub enum Action { Break, Place(u16) }

#[derive(Debug, Clone, Copy)]
pub struct Edit { pub sequence: u64, pub position: Pos, pub action: Action }

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Reject { Replay, Mode, Range, InvalidBlock, Occupied, Unsupported, Inventory, Capacity, BadSnapshot }

#[derive(Debug)]
pub struct Engine { pub world: World, pub tick: u64 }

impl Engine {
    pub fn new(seed: u64) -> Self { Self { world: World::new(seed), tick: 0 } }
    pub fn step(&mut self) { self.tick = self.tick.saturating_add(1); }

    /// Applies a minimal authoritative block transaction. This slice intentionally
    /// does not yet implement ray occlusion, mining time, tool gates or character collision.
    pub fn edit(&mut self, player: &mut Player, edit: Edit) -> Result<(), Reject> {
        if edit.sequence <= player.last_sequence { return Err(Reject::Replay); }
        if player.mode == Mode::Spectator { return Err(Reject::Mode); }
        let p = edit.position;
        if !p.valid() || p.y == 0 { return Err(Reject::InvalidBlock); }
        let center = [p.x as i64 * 1000 + 500, p.y as i64 * 1000 + 500, p.z as i64 * 1000 + 500];
        let delta = [center[0] as i128 - player.position_mm[0] as i128,
                     center[1] as i128 - player.position_mm[1] as i128,
                     center[2] as i128 - player.position_mm[2] as i128];
        if delta.iter().any(|d| d.abs() > 6000) ||
            delta.iter().map(|d| d*d).sum::<i128>() > 36_000_000 {
            return Err(Reject::Range);
        }
        let old = self.world.block(p);
        match edit.action {
            Action::Break => {
                if !solid(old) || old == BEDROCK { return Err(Reject::InvalidBlock); }
                if player.mode != Mode::Creative && player.count(old) == u32::MAX {
                    return Err(Reject::Capacity);
                }
                self.world.set(p,AIR)?;
                if player.mode != Mode::Creative { player.grant(old,1)?; }
            }
            Action::Place(b) => {
                if !known(b) || !solid(b) || b == BEDROCK { return Err(Reject::InvalidBlock); }
                if old != AIR { return Err(Reject::Occupied); }
                let neighbors = [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)];
                if !neighbors.iter().any(|&(x,y,z)| solid(self.world.block(Pos{x:p.x+x,y:p.y+y,z:p.z+z}))) {
                    return Err(Reject::Unsupported);
                }
                let count = player.count(b);
                if player.mode != Mode::Creative && count == 0 { return Err(Reject::Inventory); }
                self.world.set(p,b)?;
                if player.mode != Mode::Creative {
                    if count == 1 { player.inventory.remove(&b); }
                    else { player.inventory.insert(b,count-1); }
                }
            }
        }
        player.last_sequence = edit.sequence;
        Ok(())
    }
}

/// Small stable ABI entry points for upcoming Android/JNI and Wasm adapters.
#[no_mangle]
pub extern "C" fn frontier_height(seed: u64, x: i32, z: i32) -> i32 {
    if !(-LIMIT..=LIMIT).contains(&x) || !(-LIMIT..=LIMIT).contains(&z) { return -1; }
    height(seed,x,z)
}

#[no_mangle]
pub extern "C" fn frontier_block(seed: u64, x: i32, y: i32, z: i32) -> u16 {
    let p = Pos{x,y,z};
    if !p.valid() { return u16::MAX; }
    generated(seed,p)
}
