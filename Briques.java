//Jeu de Casse-Briques

package projet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

class Briques extends JFrame
{
	//attributs
	int nbCasesX=40;
	int nbCasesY=30;
	int largeur=20;
	int marge=40;
	ArrayList<Cellule> briques;
	ArrayList<Cellule> obstacle;
	Cellule ball;
	ArrayList<Cellule> barre;
	ArrayList<Cellule> bordure;
	JButton replay, exit;
	int score=0;
	int stage=2;
	boolean perte=false;
	int direction=0; //0=none(default) 1=right 2=left
	Timer timer;
	int freq=10;
	int dx=2,dy=-4; //vitesse de deplacement selon x et selon y (en pixels)
	int indice=100; //pour ralentire la frequence du chhangement de couleur de la bordure
	
	JPanel panel = new JPanel() 
	{
		@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		super.paint(g);
		setBackground(Color.black);
		
		//dessiner les differentes composantes
		dessinerBall(g);
		dessinerBarre(g);
		dessinerBriques(g);
		dessinerBordure(g);
		dessinerObstacle(g);
		//score && stage
		g.setFont(new Font("Times New Roman", 1, 20));
		g.setColor(new Color(250,200,250));
		g.drawString("Stage: "+stage, getWidth()/4, 20);
		g.drawString("Score: "+score, (int) (getWidth()*(3.0/4)), 20);
		
		//on change le panneau lorsqu'on perd
		if(perte)
		{
			setBackground(Color.red);
			g.setColor(new Color(250,200,250));
			g.drawString("Vous avez perdu!", (int) (getWidth()/3.0), getHeight()/2);
			this.add(replay);
			this.add(exit);
		}
		if(stage==2 && briques.isEmpty())
		{
			this.removeAll();
			timer.stop();
			setBackground(Color.blue);
			g.setColor(new Color(250,120,120));
			g.setFont(new Font("Courier New", Font.BOLD, 30));
			g.drawString("Vous avez termine le jeu! Felicitations.",5*largeur, 12*largeur);
		}
	}};
//le constructeur
	public Briques()
	{
		this.setTitle("Jeux de Briques");
		this.setSize(2*marge+largeur*nbCasesX,2*marge+largeur*nbCasesY);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
//		this.setLocationRelativeTo(null);
		this.setLocation(0,0);
		
		//initialiser les differentes composantes
		initialiserBarre();
		initialiserBall();
		initialiserBriquesEnStage(stage);
		initialiserBordure();
		
		//determiner la direction de barre
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				super.keyPressed(e);
				if(e.getKeyCode()==KeyEvent.VK_RIGHT) direction=1;
				if(e.getKeyCode()==KeyEvent.VK_LEFT) direction=2;
				if(e.getKeyCode()==KeyEvent.VK_SPACE)
				{
					if(timer.isRunning()) timer.stop();
					else timer.restart();
				}
			}
		});
		
		//les boutons replay && exit
		replay = new JButton("REPLAY");
		exit = new JButton("EXIT");
		replay.setLocation((int) (getWidth()/4.0)-100, getHeight()/2+20);
		exit.setLocation((int) (getWidth()*(3.0/4))-150, getHeight()/2+20);
		replay.setSize(200,100); exit.setSize(200,100);
		
		replay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dispose();
				new Briques();
			}
		});
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dispose();
			}
		});
		
		//l'horloge
		timer = new Timer(freq, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//le mouvement de la ball
				ball.x+=dx;
				ball.y+=dy;
				
				//le mouvement de la barre
				if(direction==1 && barre.get(barre.size()-1).x<getWidth()-largeur)
				{
					for(Cellule c: barre)
					{
						c.x+=(int)((largeur/25.0)*freq); //pour mettre la vitesse de la barre en fonction de
						//la frequence
					}
				}
				if(direction==2 && barre.get(0).x>0)
				{
					for(Cellule c: barre)
					{
						c.x-=(int)((largeur/25.0)*freq);
					}
				}
				
				//la ball touche la barre
				//on change la direction de la ball vers le haut
					if(ball.x>=barre.get(0).x && ball.x<=barre.get(barre.size()-1).x && ball.y==getHeight()-5*largeur)  dy*=-1;
				
				//la ball touche les frontieres droites et gauches
				if(ball.x>=getWidth()-largeur && dx>0)
				{
					dx*=-1;	//on change la direction vers la droite
				}
				if(ball.x<=0 && dx<0)
				{
					dx*=-1;	//on change la direction vers la gauche
				}
				//la ball touche la frontiere en haut
				if(ball.y<=0 && dy<0)
				{
					dy*=-1; //on change la direction vers le bas
				}
				//la ball touche la frontiere en bas : on perd!
				if(ball.y>=getHeight()-1)
				{
					perte=true;
					timer.stop();
				}
				
				//la ball touche les briques
				for(int i=0;i<briques.size();i++)
				{
						if(new Rectangle(briques.get(i).x, briques.get(i).y, 4*largeur, largeur).intersects(new Rectangle(ball.x, ball.y, largeur, largeur)))
						{
							briques.remove(i);
							dy*=-1; score+=10;
						}
				}
				
				//la ball touche l'obstacle
				if(!obstacle.isEmpty())
				{
					for(Cellule c: obstacle)
					{
						if(new Rectangle(c.x,c.y,c.longueur,c.hauteur).intersects(new Rectangle(ball.x,ball.y,largeur, largeur)))
						{
							dx*=-1; dy*=-1;
						}
					}
				}
				
				//selectionner le stage
				if(stage==1 && briques.isEmpty())
				{
					stage=2;
					initialiserBriquesEnStage(2);
					initialiserBall();
				}
				
				
				//jouer avec les couleurs de la bordure
				Random r=new Random();
				if(indice==100)
				{
					for(Cellule c: bordure)
					{
						
							c.couleur=new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
					}
					indice=1;
				}
				else indice++;

				repaint();
			}
		});
		timer.start();
		this.setContentPane(panel);
		
		
		this.setVisible(true);
	}//fin constructeur
	
	//les methodes d'initialisation
	void initialiserBarre()
	{
		 barre = new ArrayList<Cellule>();
			for(int i=getWidth()/4; i<getWidth()*(3.0/4);i+=largeur)
			{
				barre.add(new Cellule(i, getHeight()-4*largeur, largeur, largeur, Color.cyan));
			}
	}
	void initialiserBall()
	{
		ball = new Cellule(getWidth()/2, getHeight()/2, largeur, largeur, Color.red);
	}
	void initialiserBriquesEnStage(int s) //intialiser les briques selon le stage
	{
		briques = new ArrayList<Briques.Cellule>();
		obstacle = new ArrayList<Briques.Cellule>();
		
		if(s==1) //stage 1
		{
			for(int i=marge; i<getWidth()-marge; i+=5*largeur)
			{
				for(int j=marge;j<8*largeur; j+=2*largeur)
				{
				briques.add(new Cellule(i, j, 4*largeur, largeur, Color.yellow));
				}
			}
		}
		if(stage==2) //stage 2
		{
			int n=1;
			for(int j=marge;j<10*largeur;j+=2*largeur)
			{
				for(int i=0;i<n;i++)
				{
					briques.add(new Cellule(getWidth()/2-5*i*largeur, j, 4*largeur, largeur, Color.white));
					if(i<n-1)
					briques.add(new Cellule(getWidth()/2+5*(i+1)*largeur, j, 4*largeur, largeur, Color.white));
				} n++;
			}
		//ajouter un obstacle en stage 2
			obstacle.add(new Cellule(0,12*largeur,5*largeur,largeur, Color.orange));
			obstacle.add(new Cellule(getWidth()-5*largeur,12*largeur,5*largeur,largeur, Color.orange));
		}
	}
	void initialiserBordure() //colorer la bordure de la fenetre
	{		
		bordure = new ArrayList<Cellule>();
		for(int i=0;i<getWidth()-10;i+=20)
		{
			bordure.add(new Cellule(i,0,10,2,Color.red));
			bordure.add(new Cellule(i,getHeight()-50,10,2,Color.red));
			
		}
		for(int j=0;j<getHeight()-40;j+=20)
		{
			bordure.add(new Cellule(0,j,2,10,Color.red));
			bordure.add(new Cellule(getWidth()-2,j,2,10,Color.red));
		}
	}
	
	//les methodes de dessin
	void dessinerRectCellule(int x, int y, int l, int h, Color c, Graphics g) //dessiner une cellule rectangle
	{
		g.setColor(c);
		g.fillRect(x, y, l, h);
	}
	void dessinerRectCellule(Cellule c, Graphics g)
	{
		dessinerRectCellule(c.x, c.y, c.longueur, c.hauteur, c.couleur, g);
	}
	void dessinerOvalCellule(int x, int y, int l, int h, Color c, Graphics g) //dessiner une cellule cercle
	{
		g.setColor(c);
		g.fillOval(x, y, l, h);
	}
	void dessinerOvalCellule(Cellule c, Graphics g)
	{
		dessinerOvalCellule(c.x, c.y, c.longueur, c.hauteur, c.couleur, g);
	}
	
	void dessinerBall(Graphics g) //projectile
	{
		dessinerOvalCellule(ball, g);
	}
	
	void dessinerBarre(Graphics g) //la barre qui frappe la ball
	{
		for(Cellule c: barre)
		{
			dessinerRectCellule(c, g);
		}
	}
	void dessinerBriques(Graphics g)
	{
		for(Cellule c: briques)
		{
			dessinerRectCellule(c, g);
		}
	}
	void dessinerObstacle(Graphics g)
	{
		if(!obstacle.isEmpty())
		{
			for(Cellule c: obstacle)
			{
				dessinerRectCellule(c, g);
			}
		}
	}
	void dessinerBordure(Graphics g)
	{
		for(Cellule c: bordure)
		{
			dessinerRectCellule(c, g);
		}
	}
	
	// la fonction main
public static void main(String [] args)
{
	new Briques();
}


//la classe cellule
class Cellule
{
	int x,y; //cordonnees
	int longueur, hauteur;
	Color couleur;

	public Cellule(int x, int y, int longueur, int hauteur, Color couleur) {
		super();
		this.x = x;
		this.y = y;
		this.longueur=longueur;
		this.hauteur=hauteur;
		this.couleur = couleur;
	}
}
}

	
