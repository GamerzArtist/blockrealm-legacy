// Frontier cross-platform reference contract; not the live game engine.
export const PROTOCOL=1, TICK_HZ=20, CHUNK=16, HEIGHT=384;

export function address(x,y,z) {
  if (![x,y,z].every(Number.isSafeInteger) || y<0 || y>=HEIGHT) throw RangeError('invalid block position');
  const cx=Math.floor(x/CHUNK), cz=Math.floor(z/CHUNK);
  return {cx,cz,lx:x-cx*CHUNK,y,lz:z-cz*CHUNK};
}

export function compatible(local,remote) {
  if (!local||!remote||local.protocol!==PROTOCOL||remote.protocol!==PROTOCOL||
      typeof local.generator!=='string'||!local.generator||local.generator!==remote.generator) return false;
  const valid=x=>typeof x==='string'&&/^[a-f0-9]{64}$/.test(x);
  if (!valid(local.registry)||local.registry!==remote.registry||
      !Array.isArray(local.mods)||!Array.isArray(remote.mods)||
      !local.mods.every(valid)||!remote.mods.every(valid)) return false;
  const a=[...local.mods].sort(),b=[...remote.mods].sort();
  return new Set(a).size===a.length&&new Set(b).size===b.length&&
         a.length===b.length&&a.every((x,i)=>x===b[i]);
}

// Validates the message envelope only. A real server must ALSO check line of sight,
// block registry membership, inventory, cooldowns, occupancy and permissions.
export function validateEdit(edit,player,lastSequence) {
  if (!edit||!player||!Number.isSafeInteger(lastSequence)||
      !Number.isSafeInteger(edit.sequence)||edit.sequence<=lastSequence||
      !['survival','creative','hardcore'].includes(player.mode)) return false;
  try {address(edit.x,edit.y,edit.z)} catch {return false}
  if (![player.x,player.y,player.z].every(Number.isFinite)||
      !Number.isInteger(edit.block)||edit.block<0||edit.block>65535) return false;
  return Math.hypot(edit.x+.5-player.x,edit.y+.5-player.y,edit.z+.5-player.z)<=6;
}
