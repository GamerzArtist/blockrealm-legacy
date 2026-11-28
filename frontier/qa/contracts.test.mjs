import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {address,compatible,validateEdit,TICK_HZ} from '../core/contracts.mjs';
const hash='a'.repeat(64),other='b'.repeat(64),hello={protocol:1,generator:'frontier-1',registry:hash,mods:[hash,other]};
test('negative chunks and vertical limits',()=>{
  assert.deepEqual(address(-1,383,-17),{cx:-1,cz:-2,lx:15,y:383,lz:15});
  assert.throws(()=>address(0,384,0)); assert.throws(()=>address(.5,0,0)); assert.equal(TICK_HZ,20);
});
test('version and content mismatch rejected',()=>{
  assert(compatible(hello,{...hello,mods:[other,hash]}));
  assert(!compatible(hello,{...hello,protocol:2}));
  assert(!compatible(hello,{...hello,registry:other}));
  assert(!compatible(hello,{...hello,mods:[hash,hash]}));
  assert(!compatible(hello,{...hello,mods:null}));
});
test('edit envelope rejects replay, spectators and range violations',()=>{
  const p={x:0,y:64,z:0,mode:'survival'},e={x:1,y:64,z:0,block:2,sequence:1};
  assert(validateEdit(e,p,0));assert(!validateEdit(e,p,1));assert(!validateEdit(e,{...p,mode:'spectator'},0));
  assert(!validateEdit({...e,x:20},p,0));assert(!validateEdit({...e,block:-1},p,0));
});
const s=readFileSync('web/game.js','utf8');
test('legacy web and packaged Android sources match',()=>{
  for(const file of ['game.js','index.html','style.css'])
    assert.equal(readFileSync('web/'+file,'utf8'),readFileSync('android/app/src/main/assets/'+file,'utf8'));
});
test('save restoration ignores injected and invalid fields',()=>{
  const fn=s.split('\n').find(l=>l.startsWith('function restoreKnown('));
  assert(fn);
  const result=runInNewContext(fn+";const t={x:0,hp:20};restoreKnown(t,JSON.parse('{\"x\":12,\"hp\":\"bad\",\"__proto__\":{\"polluted\":true},\"constructor\":1,\"unexpected\":1}'));JSON.stringify({t,polluted:t.polluted})");
  assert.deepEqual(JSON.parse(result),{t:{x:12,hp:20}});
});
test('debug HUD uses text sink and accessible pause exists',()=>{
  assert.match(s,/dom\.debug\.textContent=/);
  assert.doesNotMatch(s,/dom\.debug\.innerHTML\s*=/);
  assert.match(readFileSync('web/index.html','utf8'),/aria-label="Pause game"/);
});
test('uniform lookup cached per program, including null results',()=>{
  const fn=s.split('\n').find(l=>l.startsWith('function uni(p,n)'));
  assert(fn);
  const count=runInNewContext('let calls=0;const gl={getUniformLocation(){calls++;return null}};const uniformLocations=new WeakMap();'+fn+';const a={},b={};uni(a,"u");uni(a,"u");uni(b,"u");calls');
  assert.equal(count,2);
});
