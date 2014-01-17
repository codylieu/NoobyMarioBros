package gameproject;
import jgame.*;
import jgame.platform.*;

public class Game extends StdGame {

	public static void main(String[] args){
		new Game(new JGPoint(640, 480));
	}
	public Game(JGPoint size){
		initEngine(size.x, size.y);
	}
	public Game(){
		initEngineApplet();
	}
	public void initCanvas(){
		setCanvasSettings(40, 30, 16, 16, null, null, null);
	}
	public void initGame(){
		setFrameRate(45, 2);
		defineMedia("game.tbl");
		setBGImage("mybackground");
		try{
			Thread.sleep(4000);
		}
		catch(InterruptedException e){};
		setHighscores(10, new Highscore(0, "nobody"),25);
	}
	public void initNewLife(){
		defineLevel();
	}
	public void startGameOver(){
		removeObjects(null, 0);
		if(!gameWon)
			playAudio("gameOver");
	}
	public void incrementLevel() {
		if(level < 2)
			level++;
		stage++;
	}
	public void defineLevel(){
		removeObjects(null, 0);
		new Player(pfWidth()-640,pfHeight()/2,5);
		if(level == 0)
			minionLevel();
		else if(level == 1)
			bossLevel();
	}
	public void minionLevel(){
		for(int i = 0; i < 20; i++)
			new Enemy();
		if (gametime>= 200 && countObjects("enemy",0)==0)
			levelDone();
	}
	public void bossLevel(){
		removeObjects(null, 0);
		new Player(pfWidth()-640,pfHeight()/2,5);
		new Boss();
	}
	public void paintFrameInGame(){
		setFont(new JGFont("arial", 0, 15));
		if(level == 0)
			drawString("Defeat the Koopa Minions to face Bowser!!", pfWidth()/2, 40, 0);
		else if(level == 1){
			drawString("Focus on taking down Bowser! NOT the Bullet Bills", pfWidth()/2, 20, 0);
			drawString("It'll take more than just one hit to take him down", pfWidth()/2, 40, 0);
		}
	}
	int BULLET_BILL_DURATION = 6400;
	int BULLET_BILL_FREQUENCY = 12;
	public void doFrameInGame(){
		moveObjects(null, 0);
		moveObjects();
		checkCollision(2,1); // enemies hit player
		checkCollision(4,2); // bullets hit enemies
		checkCollision(4,10); // bullet hits boss
		if(level == 1){
			checkCollision(16,1);
			checkCollision(4,16);
			if (checkTime(0,(int)(BULLET_BILL_DURATION),(int)((BULLET_BILL_FREQUENCY))))
				new BulletBill();
		}
		if((level == 0) && (gametime>= 200 && countObjects("enemy",0)==0)){
			levelDone();
		}
		if((level == 1) && (gametime>= 200 && countObjects("boss",0)==0)){
			gameOver();
		}
		// for debugging purposes
		if(getKey('Q')){
			levelDone();
		}
	}

	boolean gameWon = false;
	boolean FIREBALL_CHEAT = false;
	int AMMO;
	boolean AMMO_CHEAT = false;
	boolean INVINCIBILITY = false;

	public class Player extends JGObject {
		public Player(double x,double y,double speed) {
			super("player",true,x,y,1,"mario", 0,0,speed,speed,-1);
		}
		public void move() {
			setDir(0,0);
			if (getKey(key_up)  && y > yspeed){
				ydir=-1;
			}
			if (getKey(key_down) && y < pfHeight()-55-xspeed){
				ydir=1;
			}
			if(level == 0){
				if (getKey(key_left) && x > xspeed){
					xdir=-1;
				}
				if (getKey(key_right) && x < pfWidth()-30-xspeed){
					xdir=1;
				}
			}
			// Allows bigger projectile that doesn't remove after hitting a target,
			// so it can hit multiple targets
			if(getKey('X')){
				FIREBALL_CHEAT = !FIREBALL_CHEAT;
				clearKey('X');
			}
			// Increases the amount of fireballs allowed to be fired before expiring
			// and makes it so that you just have to keep pressing down on the key to fire
			if(getKey('C')){
				AMMO_CHEAT = !AMMO_CHEAT;
				clearKey('C');
			}
			if(AMMO_CHEAT)
				AMMO = 100;
			else
				AMMO = 2;
			// Similar to Star Power in Mario, allows player to become invulnerable and
			// kill enemies that he touches
			if(getKey('V')){
				INVINCIBILITY = !INVINCIBILITY;
				clearKey('V');
			}
			if (getKey('Z') && countObjects("bullet",0) < AMMO) { // AMMO cheat code to increase amount of shots at once
				if(FIREBALL_CHEAT){
					new JGObject("bullet", true, x, y, 4, "OPfireball", 5, 0, -2); // OP = over powered
				}
				else{
					new JGObject("bullet", true, x, y, 4, "fireball", 5, 0 , -2);
				}
				playAudio("fireSound");
				if(!AMMO_CHEAT)
					clearKey('Z'); // AMMO cheat code so you can just keep your finger on the key to fire continuously
			}
		}
		public void hit(JGObject obj) {
			if (and(obj.colid,2+16) && !INVINCIBILITY){
				playAudio("deathSound");
				lifeLost();
			}
			else {
				score += 5;
				obj.remove();
			}
		}
	}

	public class Enemy extends JGObject {
		double timer=0;
		public Enemy() {
			super("enemy",true, 580,random(32, pfHeight()-40), 2, "minion", random(-3, 3), random(-3, 3), -2);
		}
		public void move() {
			if((x > pfWidth()-50) || (x < 0)){
				xspeed = -xspeed;
			}
			if((y > pfHeight()-50) || (y < 0)){
				yspeed = -yspeed;
			}
		}
		public void hit(JGObject o) {
			remove();
			if(!FIREBALL_CHEAT)
				o.remove(); // FIREBALL cheat code so fireballs don't disappear after impact and can hit multiple targets
			score += 5;
		}
	}

	public class Boss extends JGObject{
		public int BOSS_HEALTH = 10;
		public Boss(){
			super("boss", true, 520, pfHeight()/2, 10, "bowser", 0, random(-5, 5), -2);
		}
		public void move(){
			if((x > pfWidth()-100) || (x < 0)){
				xspeed = -xspeed;
			}
			if((y > pfHeight()-100) || (y < 0)){
				yspeed = -yspeed;
			}
		}
		public void hit(JGObject o){
			o.remove();
			BOSS_HEALTH--;
			if(BOSS_HEALTH==0){
				remove();
				score+=500;
				playAudio("winSound");
				gameWon = true;
			}
		}
	}
	public class BulletBill extends JGObject{
		public BulletBill(){
			super("bill", true, 580,random(32, pfHeight()-40), 16, "bulletbill", -5, 0);
		}
		public void hit(JGObject o){
			remove();
			if(!FIREBALL_CHEAT)
				o.remove();
			score+=10;
		}
	}
}
