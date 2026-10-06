/* Traffic Mania — deterministic puzzle engine. Also used by the test runner. */
(function(root){
'use strict';
const DIRS=[[1,0],[0,1],[-1,0],[0,-1]];
function rng(seed){return function(){let t=seed+=0x6D2B79F5;t=Math.imul(t^t>>>15,t|1);t^=t+Math.imul(t^t>>>7,t|61);return ((t^t>>>14)>>>0)/4294967296;};}
function cells(v){return Array.from({length:v.len},(_,i)=>[v.x+(v.dir%2===0?i:0),v.y+(v.dir%2===1?i:0)]);}
function occupancy(level, vehicles=level.cars){let grid=new Map();for(const o of level.obstacles)grid.set(o.x+','+o.y,-1);for(const v of vehicles)for(const [x,y]of cells(v))grid.set(x+','+y,v.id);return grid;}
function canExit(level,v,vehicles=level.cars){const grid=occupancy(level,vehicles);const [dx,dy]=DIRS[v.dir];let x=v.x+(dx===1?v.len-1:0), y=v.y+(dy===1?v.len-1:0);x+=dx;y+=dy;while(x>=0&&y>=0&&x<level.size&&y<level.size){if(grid.has(x+','+y))return false;x+=dx;y+=dy;}return true;}
function solve(level){let rest=level.cars.slice(), order=[];while(rest.length){const v=rest.find(c=>canExit(level,c,rest));if(!v)return null;order.push(v.id);rest=rest.filter(c=>c.id!==v.id);}return order;}
function generate(n){if(!Number.isInteger(n)||n<1||n>500)throw Error('Nivel inválido');const random=rng(n*93017+71237),size=n<8?5:n<45?6:n<140?7:n<300?8:9;
const level={number:n,size,theme:Math.min(5,Math.floor((n-1)/84)),cars:[],obstacles:[]};
const obstacleCount=n<15?0:Math.min(7,1+Math.floor(n/75));
for(let i=0;i<obstacleCount;i++){const x=1+Math.floor(random()*(size-2)),y=1+Math.floor(random()*(size-2));if(!level.obstacles.some(o=>o.x===x&&o.y===y))level.obstacles.push({x,y,type:i%2?'barrier':'tree'});}
const target=n<4?3+n:Math.floor(size*size*.36);
for(let attempt=0;attempt<3500&&level.cars.length<target;attempt++){
 const dir=Math.floor(random()*4),len=n>12&&random()<.19?3:n>6&&random()<.17?1:2;
 const v={id:level.cars.length,x:Math.floor(random()*size),y:Math.floor(random()*size),dir,len,color:Math.floor(random()*6),type:len===1?'moto':len===3?(random()<.5?'bus':'truck'):(n>20&&random()<.18?'ambulance':'car')};
 const grid=occupancy(level);if(cells(v).some(([x,y])=>x>=size||y>=size||grid.has(x+','+y)))continue;
 // The newest car always has an exit. Removing in reverse insertion order proves solvability.
 if(canExit(level,v))level.cars.push(v);
}
return level;}
const themes=[{name:'Ciudad',tag:'DISTRITO CENTRAL',bg:'#adcfb0',road:'#667483',accent:'#69dfb4',icon:'🏙️'},{name:'Supermercado',tag:'LA GRAN COMPRA',bg:'#cbd5ac',road:'#77808a',accent:'#ffcd69',icon:'🛒'},{name:'Playa',tag:'COSTA DEL SOL',bg:'#e7cf97',road:'#75858b',accent:'#56d8e6',icon:'🏖️'},{name:'Aeropuerto',tag:'TERMINAL NORTE',bg:'#a9c9d8',road:'#626e80',accent:'#a7b8ff',icon:'✈️'},{name:'Mall',tag:'PLAZA CENTRAL',bg:'#c7b5cf',road:'#747087',accent:'#e6a3e4',icon:'🛍️'},{name:'Noche',tag:'CIUDAD NEÓN',bg:'#324953',road:'#41455f',accent:'#7bf2ca',icon:'🌃'}];
const skins=[{id:'classic',name:'Original',price:0,colors:['#ffbb4d','#f27475','#70c9ee','#bd9ced','#80d2ad','#e8e7e2']},{id:'ocean',name:'Océano',price:180,colors:['#26c6da','#4f9ce8','#79e4d9','#365ec6','#8cceef','#d2f6ef']},{id:'candy',name:'Caramelo',price:350,colors:['#f9a2cc','#ce9efa','#fdc691','#eb7bac','#a9a9ff','#fff0d6']},{id:'racing',name:'Racing',price:650,colors:['#fb4c57','#f2c744','#efefef','#4388d8','#f48236','#91d469']},{id:'night',name:'Neón',price:1000,colors:['#a583ff','#4be3ce','#ff76b1','#d1f46a','#74bbff','#e5a6ff']},{id:'gold',name:'Colección oro',price:1600,colors:['#ecc671','#dcae43','#ffe3a4','#d9b476','#f0d899','#ba9048']}];
function defaults(){return {version:1,coins:100,unlocked:1,stars:{},best:{},owned:['classic'],skin:'classic',dailyDate:0,dailyStreak:0,streak:0,maxStreak:0,hints:3,sound:true,haptic:true,reduced:false,session:null};}
function normalize(raw){const d=defaults();if(!raw||typeof raw!=='object')return d;for(const k of ['coins','unlocked','dailyDate','dailyStreak','streak','maxStreak','hints'])if(Number.isFinite(raw[k]))d[k]=Math.max(0,Math.floor(raw[k]));d.unlocked=Math.min(500,Math.max(1,d.unlocked));for(const k of ['sound','haptic','reduced'])if(typeof raw[k]==='boolean')d[k]=raw[k];for(let n=1;n<=500;n++){const s=raw.stars?.[n];if(Number.isInteger(s)&&s>=1&&s<=3)d.stars[n]=s;const b=raw.best?.[n];if(b&&Number.isFinite(b.time)&&b.time>=0&&Number.isInteger(b.errors)&&b.errors>=0)d.best[n]={time:b.time,errors:b.errors};}d.owned=skins.filter(s=>s.id==='classic'||raw.owned?.includes(s.id)).map(s=>s.id);d.skin=d.owned.includes(raw.skin)?raw.skin:'classic';if(raw.session&&Number.isInteger(raw.session.level)&&raw.session.level<=d.unlocked&&raw.session.level>=1&&Array.isArray(raw.session.removed)){const l=generate(raw.session.level);const ids=raw.session.removed;let cars=l.cars.slice(),valid=true;for(const id of ids){const v=cars.find(c=>c.id===id);if(!v||!canExit(l,v,cars)){valid=false;break;}cars=cars.filter(c=>c.id!==id);}if(valid&&cars.length)d.session={level:l.number,removed:ids.slice(),errors:Math.max(0,Number(raw.session.errors)||0),elapsed:Math.max(0,Number(raw.session.elapsed)||0)};}return d;}
function dayKey(date=new Date()){return Math.floor(Date.UTC(date.getFullYear(),date.getMonth(),date.getDate())/86400000);}
function claimDaily(s,today=dayKey()){if(today<=s.dailyDate)return 0;s.dailyStreak=today===s.dailyDate+1?s.dailyStreak%7+1:1;const reward=[30,40,50,65,80,100,150][s.dailyStreak-1];s.dailyDate=today;s.coins+=reward;return reward;}
function complete(s,n,errors,seconds){const stars=errors===0?3:errors<=2?2:1,old=s.stars[n]||0,first=!old;const coins=first?20+stars*5:Math.max(0,stars-old)*5;s.coins+=coins;s.stars[n]=Math.max(old,stars);s.unlocked=Math.max(s.unlocked,Math.min(500,n+1));if(!s.best[n]||errors<s.best[n].errors||(errors===s.best[n].errors&&seconds<s.best[n].time))s.best[n]={time:seconds,errors};if(first){s.streak=errors===0?s.streak+1:0;s.maxStreak=Math.max(s.maxStreak,s.streak);}s.session=null;return {stars,coins,first};}
const api={DIRS,rng,cells,occupancy,canExit,solve,generate,themes,skins,defaults,normalize,dayKey,claimDaily,complete};if(typeof module!=='undefined')module.exports=api;root.TrafficEngine=api;
})(typeof window!=='undefined'?window:globalThis);
