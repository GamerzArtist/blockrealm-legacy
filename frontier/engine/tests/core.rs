use frontier_core::*;

#[test]
fn negative_chunk_and_vertical_bounds() {
    assert_eq!(Pos{x:-1,y:383,z:-17}.address(),Some((-1,-2,383*256+15*16+15)));
    assert_eq!(Pos{x:0,y:384,z:0}.address(),None);
    assert_eq!(frontier_block(0,0,-1,0),u16::MAX);
}

#[test]
fn chunk_matches_point_queries() {
    for seed in [0,1,42,u64::MAX] {
        let w=World::new(seed);
        let c=w.chunk(-1,2).unwrap();
        assert_eq!(c.len(),16*16*384);
        for y in [0,1,40,60,64,90,383] {
            for z in 32..48 {
                for x in -16..0 {
                    let p=Pos{x,y,z};
                    assert_eq!(c[p.address().unwrap().2],w.block(p));
                }
            }
        }
        assert_eq!(c,w.chunk(-1,2).unwrap());
    }
    assert!(World::new(1).chunk(i32::MAX,0).is_none());
}

#[test]
fn survival_transaction_and_snapshot() {
    let mut e=Engine::new(123);
    let y=height(123,0,0);
    let pos=Pos{x:0,y,z:0};
    let mut p=Player::new(Mode::Survival,[500,y as i64*1000+1500,500]);
    let old=e.world.block(pos);
    e.edit(&mut p,Edit{sequence:1,position:pos,action:Action::Break}).unwrap();
    assert_eq!(e.world.block(pos),AIR);
    assert_eq!(p.count(old),1);
    let bytes=e.world.encode();
    assert_eq!(World::decode(&bytes).unwrap(),e.world);
    assert_eq!(e.world.chunk(0,0).unwrap()[pos.address().unwrap().2],AIR);
    assert_eq!(e.edit(&mut p,Edit{sequence:1,position:pos,action:Action::Place(old)}),Err(Reject::Replay));
    e.edit(&mut p,Edit{sequence:2,position:pos,action:Action::Place(old)}).unwrap();
    assert_eq!(p.count(old),0);
    assert_eq!(e.world.encode().len(),16);
}

#[test]
fn reject_out_of_range_spectator_and_invalid_inventory() {
    let mut e=Engine::new(42);
    let y=height(42,0,0);
    let pos=Pos{x:0,y,z:0};
    let mut p=Player::new(Mode::Spectator,[500,y as i64*1000+500,500]);
    let a=Edit{sequence:1,position:pos,action:Action::Break};
    assert_eq!(e.edit(&mut p,a),Err(Reject::Mode));
    p.mode=Mode::Survival;
    p.position_mm=[i64::MIN,i64::MAX,0];
    assert_eq!(e.edit(&mut p,a),Err(Reject::Range));
    p.position_mm=[500,y as i64*1000+500,500];
    assert_eq!(e.edit(&mut p,Edit{sequence:1,position:pos,action:Action::Place(999)}),Err(Reject::InvalidBlock));
    assert_eq!(p.count(STONE),0);
}

#[test]
fn malformed_snapshots_rejected() {
    let mut bytes=World::new(4).encode();
    bytes[12]=1;
    assert_eq!(World::decode(&bytes),Err(Reject::BadSnapshot));
    assert_eq!(World::decode(b"bad"),Err(Reject::BadSnapshot));
}

#[test]
fn creative_and_tick_behavior() {
    let mut e=Engine::new(19);
    let y=height(19,0,0);
    let pos=Pos{x:0,y,z:0};
    let old=e.world.block(pos);
    let mut p=Player::new(Mode::Creative,[500,y as i64*1000+500,500]);
    e.edit(&mut p,Edit{sequence:1,position:pos,action:Action::Break}).unwrap();
    e.edit(&mut p,Edit{sequence:2,position:pos,action:Action::Place(old)}).unwrap();
    assert_eq!(p.count(old),0);
    for _ in 0..TICK_HZ { e.step(); }
    assert_eq!(e.tick,20);
}
