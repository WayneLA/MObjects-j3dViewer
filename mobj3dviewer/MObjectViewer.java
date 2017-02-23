package viewer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;

import components.ChangeValueEvent;
import components.ChangeValueListener;
import components.LongScrollBar;
import gui.SecondoObject;
import gui.idmanager.ID;
import sj.lang.ListExpr;
import tools.Reporter;
import viewer.hoese.CurrentState;
import viewer.hoese.Interval;
import viewer.hoese.LEUtils;
import viewer.mobject.DynamicAttribute;
import viewer.mobject.MObjQueryResult;
import viewer.mobject.MObject;
import viewer.mobject.MObjectTextPanel;

/**
 * @ClassName: MObjAttributeViewer
 * @author: wangweiwei_LA
 * @date: 2016年7月22日 下午3:45:19
 */
public class MObjectViewer extends SecondoViewer implements MouseListener{
	private static final long serialVersionUID = 1L;
	
	public static Properties configuration;
	public static final double TIMEBORDER = 0.3;
	
	public JPanel mobj3dPane = null;
	///////////////////////////////////////////////////////////////////////        AnimCtrlBar
	private LongScrollBar TimeSlider;
	private JLabel actTimeLabel;
	private AnimCtrlListener al = new AnimCtrlListener();
	///////////////////////////////////////////////////////////////////////        ViewCtrlPane
	public Button TVbtn, LVbtn, RVbtn, Resetbtn, BVbtn, Axisbtn;
	public ViewCtrlListener vl = new ViewCtrlListener();
	public boolean axis_flag = false;
	///////////////////////////////////////////////////////////////////////        ResultTxtPane
	public MObjectTextPanel resultPanel = new MObjectTextPanel(this);
	///////////////////////////////////////////////////////////////////////        MObject3dPanel
	public SimpleUniverse simpleUniverse;
	public BranchGroup branchGroup_static = new BranchGroup();
	public BranchGroup branchGroup_dynamic = new BranchGroup();
	public TransformGroup transformGroup_static = new TransformGroup();
	public TransformGroup transformGroup_dynamic = new TransformGroup();
	public BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10000.0 );

	public PickCanvas pickCanvas;
	//////////////////////////////////////////////////////////////////////         AddObject
	private boolean min_max_init = false;
	private Point3d min, max;    //the range of all mobjects
	public Vector<Interval> timeBounds;
	public Vector3d[] positions_start, positions_end; 
	public Vector<Point3d> vertices = new Vector<Point3d>(0);

	//////////////////////////////////////////////////////////////////////         move Objects
	private boolean animation = false;
	private double ani_ct;
	private int delay_val, sleep_time = 1;
	public Timer timer = null;
	public DefaultListModel listModel;
	
	//////////////////////////////////////////////////////////////////////  　　　　　***全局变量***
	private Interval TimeBounds;   			//the time bounds of all mobjects in the viewer;
	public MObjQueryResult CurrentResult;	//current object;

	public Vector list_tg_obj_d = new Vector(0);
	public Vector list_tg_obj_d_id = new Vector(0);
	
	public Vector vectors_DynamicAttributes = new Vector(0);
	
	public Vector list_tg_obj_s = new Vector(0);
	public Vector list_tg_obj_s_id = new Vector(0);
	
	public Vector vectors_mobjects = new Vector();
	
	public SecondoObject RTree_Info;
	
	
	private Vector SecondoObjects = new Vector(10, 5);
	/////////////////////////////////////////////////////////////////////			Color
	public final static Color3f COLOR_RED = new Color3f(1.0f, 0.0f, 0.0f);
	public final static Color3f COLOR_GREEN = new Color3f(0.0f, 1.0f, 0.0f);
	public final static Color3f COLOR_BLUE = new Color3f(0.0f, 0.0f, 1.0f);
	public final static Color3f COLOR_BLACK = new Color3f(0.0f, 0.0f, 0.0f);
	public final static Color3f COLOR_WRITE = new Color3f(1.0f, 1.0f, 1.0f);
	public final static Color3f COLOR_YELLOW = new Color3f(1.0f, 1.0f, 0.0f);
	public final static Color3f COLOR_CYAN = new Color3f(0.0f, 1.0f, 1.0f);
	public final static Color3f COLOR_MAGENTA = new Color3f(1.0f, 0.0f, 1.0f);
	
	public Transform3D tran = new Transform3D();
	public Vector3f VecTranslation = new Vector3f(0.0f, 0.0f, 0.0f);
	public float objScale = 0.5f;
	
	public MObjectViewer() {
		setLayout(new BorderLayout());
		initCFG();
		initMenu();
		
		JToolBar animBar = AnimCtrlBar();
		JPanel viewCtrlPane = ViewCtrlPane();
		JPanel resTxtPane = ResultTxtPane();
		mobj3dPane = createSceneGraph();
		
		JScrollPane DisplayJSPane = new JScrollPane(mobj3dPane);
		
		JSplitPane ResultViewPane, VisualPane;
		ResultViewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, viewCtrlPane, resTxtPane);
		ResultViewPane.setOneTouchExpandable(false);
		ResultViewPane.setEnabled(false);
		VisualPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ResultViewPane, DisplayJSPane);
		VisualPane.setOneTouchExpandable(true);
		VisualPane.setResizeWeight(0);
		
		JPanel mianPane = new JPanel();
		mianPane.setLayout(new BorderLayout());
		mianPane.add(animBar, BorderLayout.NORTH);
		mianPane.add(VisualPane, BorderLayout.CENTER);
		add(mianPane, BorderLayout.CENTER);
	}

	private void initMenu() {
		/** file-menu */
	}
	private void initCFG() {
		configuration = new Properties();
		String CONFIGURATION_FILE = "GBS.cfg";
		File CF = new File(CONFIGURATION_FILE);
		if (!CF.exists()) {
			Reporter.showError("HoeseViewer : configuration file not found");
			return;
		}
		try {
			FileInputStream Cfg = new FileInputStream(CF);
			configuration.load(Cfg);
			Cfg.close();
		} catch (Exception e) {
			Reporter.debug(e);
			return;
		}
	}

	private JToolBar AnimCtrlBar() {
		ImageIcon icon;
		JToolBar ctrlToolBar = new JToolBar();
		
		JToolBar AnimControlBar = new JToolBar();
		JButton ctrls[] = new JButton[7];
		String Button_File = configuration.getProperty("PlayIcon");
		if (Button_File != null) 
			ctrls[0] = new JButton(new ImageIcon(ClassLoader.getSystemResource(Button_File)));
		else ctrls[0] = new JButton(">");
		Button_File = configuration.getProperty("ReverseIcon");
		if (Button_File != null) 
			ctrls[1] = new JButton(new ImageIcon(ClassLoader.getSystemResource(Button_File)));
		else ctrls[1] = new JButton("<");
		Button_File = configuration.getProperty("ToendIcon");
		if (Button_File != null) 
			ctrls[2] = new JButton(new ImageIcon(ClassLoader.getSystemResource(Button_File)));
		else ctrls[2] = new JButton(">|");
		Button_File = configuration.getProperty("TostartIcon");
		if (Button_File != null) 
			ctrls[3] = new JButton(new ImageIcon(ClassLoader.getSystemResource(Button_File)));
		else ctrls[3] = new JButton("|<");
		Button_File = configuration.getProperty("StopIcon");
		if (Button_File != null) 
			ctrls[4] = new JButton(new ImageIcon(ClassLoader.getSystemResource(Button_File)));
		else ctrls[4] = new JButton("[]");
		
		Button_File = configuration.getProperty("SlowerIcon");
		if (Button_File != null) {
			icon = new ImageIcon(ClassLoader.getSystemResource(Button_File));
			if (icon.getImage() != null) {
				ctrls[5] = new JButton(icon);
				icon.setImage(icon.getImage().getScaledInstance(10, 10, Image.SCALE_DEFAULT));
			} 
			else ctrls[5] = new JButton("-");
		} 
		else ctrls[5] = new JButton("-");
		
		Button_File = configuration.getProperty("FasterIcon");
		if (Button_File != null) {
			icon = new ImageIcon(ClassLoader.getSystemResource(Button_File));
			if (icon.getImage() != null) {
				ctrls[6] = new JButton(icon);
				icon.setImage(icon.getImage().getScaledInstance(10, 10, Image.SCALE_DEFAULT));
			} 
			else ctrls[6] = new JButton("+");
		} 
		else ctrls[6] = new JButton("+");
		
		for (int i = 0; i < ctrls.length; i++) {
			ctrls[i].setActionCommand(Integer.toString(i));
			ctrls[i].addActionListener(al);
			AnimControlBar.add(ctrls[i]);
		}
		
		TimeSlider = new LongScrollBar(0, 0, 1);
		TimeSlider.addChangeValueListener(new TimeChangeListener());
		TimeSlider.setPreferredSize(new Dimension(400, 20));
		TimeSlider.setUnitIncrement(1000); // 1 sec
		TimeSlider.setBlockIncrement(60000); // 1 min
		
		actTimeLabel = new JLabel("No Time");
		actTimeLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
		actTimeLabel.setForeground(Color.black);
		actTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

		AnimControlBar.setFloatable(false);
		JPanel timePanel = new JPanel(new GridLayout(2, 1));
		timePanel.add(TimeSlider);
		timePanel.add(actTimeLabel);
		ctrlToolBar.add(AnimControlBar);
		ctrlToolBar.add(timePanel);
		return ctrlToolBar;
	}
	private JPanel ViewCtrlPane() {
		JPanel viewCtrlPane = new JPanel(new BorderLayout());
		JPanel ctrlPane = new JPanel(new BorderLayout());
		JPanel axisPane = new JPanel(new BorderLayout());
		
		Axisbtn = new Button("Show Axis");
		Axisbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				axis_flag = axis_flag ? false : true;
				if(axis_flag){
					//添加坐标系
					branchGroup_static.detach();
					TransformGroup stg = (TransformGroup) branchGroup_static.getChild(0);
					TransformGroup stg_other = (TransformGroup) stg.getChild(1);
					stg_other.addChild(createAxis());
					simpleUniverse.addBranchGraph(branchGroup_static);
					Axisbtn.setLabel("Cancel Axis");
				}else{
					branchGroup_static.detach();
					TransformGroup stg = (TransformGroup) branchGroup_static.getChild(0);
					TransformGroup stg_other = (TransformGroup) stg.getChild(1);
					stg_other.removeAllChildren();
					simpleUniverse.addBranchGraph(branchGroup_static);
					Axisbtn.setLabel("Show Axis");
				}
			}
		});
		axisPane.add(Axisbtn,BorderLayout.CENTER);
		
		TVbtn = new Button("Top View");
		LVbtn = new Button("L View");        RVbtn = new Button("R View");
		Resetbtn = new Button("Reset View"); BVbtn = new Button("Back View");
		LVbtn.addActionListener(vl);
		ctrlPane.add(LVbtn, BorderLayout.WEST);
		RVbtn.addActionListener(vl);
		ctrlPane.add(RVbtn, BorderLayout.EAST);
		Resetbtn.addActionListener(vl);
		ctrlPane.add(Resetbtn, BorderLayout.NORTH);
		BVbtn.addActionListener(vl);
		ctrlPane.add(BVbtn, BorderLayout.SOUTH);
		TVbtn.addActionListener(vl);
		ctrlPane.add(TVbtn, BorderLayout.CENTER);
		
		viewCtrlPane.add(axisPane, BorderLayout.NORTH);
		viewCtrlPane.add(ctrlPane, BorderLayout.CENTER);
		
		return viewCtrlPane;
	}
	private JPanel ResultTxtPane() {
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(resultPanel,BorderLayout.CENTER);
		return pane;
	}
	private JPanel createSceneGraph() {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3d = new Canvas3D(config);
		canvas3d.addMouseListener(this);
		simpleUniverse = new SimpleUniverse(canvas3d);
		simpleUniverse.getViewingPlatform().setNominalViewingTransform();
		
		//Static root branch group
		branchGroup_static.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroup_static.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		branchGroup_static.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		branchGroup_static.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		//Static root transform group
		transformGroup_static.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup_static.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup_static.setCapability(Group.ALLOW_CHILDREN_READ);
		transformGroup_static.setCapability(Group.ALLOW_CHILDREN_WRITE);
		branchGroup_static.addChild(transformGroup_static);
		
		//Dynamic root branch group
		branchGroup_dynamic.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroup_dynamic.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		branchGroup_dynamic.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		branchGroup_dynamic.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		//Dynamic root transform group
		transformGroup_dynamic = (TransformGroup)transformGroup_static.cloneNode(true);
		branchGroup_dynamic.addChild(transformGroup_dynamic);
		
		
		TransformGroup stg_obj = new TransformGroup();
		stg_obj.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		stg_obj.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		stg_obj.setCapability(Group.ALLOW_CHILDREN_READ);
		stg_obj.setCapability(Group.ALLOW_CHILDREN_WRITE);
		TransformGroup stg_other = (TransformGroup)stg_obj.cloneNode(true);
		transformGroup_static.addChild(stg_obj);
		transformGroup_static.addChild(stg_other);
		
		//light
		Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f lightDirection = new Vector3f(-1.0f, 0.0f, -1.0f);
		DirectionalLight light_static = new DirectionalLight(lightColor, lightDirection);
		light_static.setInfluencingBounds(bounds);
		branchGroup_static.addChild(light_static);
		
		DirectionalLight light_dynamic = (DirectionalLight)light_static.cloneNode(true);
		branchGroup_dynamic.addChild(light_dynamic);

		//background
//		Color3f bgColor = new Color3f(1.0f, 1.0f, 1.0f);
//		Background bg = new Background(bgColor);
//		bg.setApplicationBounds(bounds);
//		branchGroup_static.addChild(bg);
//		branchGroup_dynamic.addChild(bg);
		
		simpleUniverse.addBranchGraph(branchGroup_static);
		simpleUniverse.addBranchGraph(branchGroup_dynamic);
		
		// Create the mouse behavior node
		addMouseBehavior(branchGroup_static,transformGroup_static);
		addMouseBehavior(branchGroup_dynamic, transformGroup_dynamic);
		
		pickCanvas = new PickCanvas(canvas3d, branchGroup_dynamic);
		pickCanvas.setMode(PickTool.GEOMETRY);
		
		JPanel mobjDis = new JPanel(new BorderLayout());
		mobjDis.add(canvas3d, BorderLayout.CENTER);
		return mobjDis;
	}
	
	
	public TransformGroup createAxis(){
		TransformGroup axisTrans = new TransformGroup();
		axisTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		axisTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		axisTrans.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		axisTrans.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		axisTrans.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
		Appearance app_line = new Appearance();
		LineAttributes att_line = new LineAttributes();
		att_line.setLineWidth(1.0f);
		att_line.setLineAntialiasingEnable(false);
		app_line.setLineAttributes(att_line);
		
		// 坐标轴
		float vert[] = { -1.0f, 0.0f, 0.0f,    	1.0f, 0.0f, 0.0f, 
						 0.0f, -1.0f, 0.0f,		0.0f, 1.0f, 0.0f,
						 0.0f, 0.0f, -1.0f,    	0.0f, 0.0f, 1.0f 
					   };
		// 各定点的颜色
		float color[] = { 1.0f, 0.0f, 0.0f,    1.0f, 0.0f, 0.0f, //x-red
						  0.0f, 1.0f, 0.0f,    0.0f, 1.0f, 0.0f, //y-green
						  0.0f, 0.0f, 1.0f,    0.0f, 0.0f, 1.0f  //t-blue
					   };
		// 创建直线数组对象
		LineArray line = new LineArray(6, LineArray.COORDINATES | LineArray.COLOR_3);
		line.setCoordinates(0, vert);
		line.setColors(0, color);
		Shape3D axis = new Shape3D(line, app_line);
		
//		Transform3D pos = new Transform3D();
//		pos.setTranslation(new Vector3f(-0.5f,-0.5f,-0.5f));
//		axisTrans.setTransform(pos);
		
		axisTrans.addChild(axis);
		
		return axisTrans;
	}
	public void removeMouseBehavior(BranchGroup bg){
		bg.detach();
		bg.removeChild(4);
		bg.removeChild(3);
		bg.removeChild(2);
		if(bg.getParent() == null){
			simpleUniverse.addBranchGraph(bg);
		}
	}
	public void addMouseBehavior(BranchGroup bg, TransformGroup tg){
		bg.detach();
		MouseRotate behavior_rotate_s = new MouseRotate();
		behavior_rotate_s.setTransformGroup(tg);
		behavior_rotate_s.setSchedulingBounds(bounds);
		bg.addChild(behavior_rotate_s);
		MouseWheelZoom behavior_wheelzoom_s = new MouseWheelZoom();
		behavior_wheelzoom_s.setTransformGroup(tg);
		behavior_wheelzoom_s.setSchedulingBounds(bounds);
		bg.addChild(behavior_wheelzoom_s);
		MouseTranslate behavior_translate_s = new MouseTranslate();
		behavior_translate_s.setTransformGroup(tg);
		behavior_translate_s.setSchedulingBounds(bounds);
		bg.addChild(behavior_translate_s);
		
		if(bg.getParent() == null){
			simpleUniverse.addBranchGraph(bg);
		}
	}
	
	public String getName() {
		return "Multi-Attribute Moving Object Viewer";
	}
	public boolean isDisplayed(SecondoObject o) {
//		System.err.println("--------------------------------isDisplayed");
		return SecondoObjects.indexOf(o) >= 0;
//		return false;
	}
	public boolean canDisplay(SecondoObject o) {
//		System.err.println("--------------------------------canDisplay");
		ListExpr LE = o.toListExpr();
		ListExpr first = LE.first();
//		ListExpr second = LE.second();

//		if(first.isAtom()){
////			System.out.println("this is an atom" +"   >>>>>>>>>   "+first+"   <<<<<<<<<<");
//			if(first.symbolValue().equals("mpoint")){
//				return false;
//			}
//		} else{
////			System.out.println("this is not an atom"+"  ========  "+first+"  ========  ");
//			if(first.second().second().first().second().symbolValue().equals("rect3")){
//				return true;
//			}else{
//				return true;
//			}
//		}
		if(first.isAtom()){
//			System.out.println("this is an atom" +"   >>>>>>>>>   "+first+"   <<<<<<<<<<");
			return false;
		} 
		return true;
	}
	public void removeObject(SecondoObject o) {
//		System.out.println("removeObject");
		int index = getQueryIndex(o);
		SecondoObjects.remove(index);
		if (index >= 0) {
			int oid = o.getID().hashCode() - 1;
			
			if (list_tg_obj_s_id.indexOf(oid) != -1) {
				int in_dex = list_tg_obj_s_id.indexOf(oid);
				
				TransformGroup obj_s = (TransformGroup) list_tg_obj_s.get(in_dex);
				branchGroup_static.detach();
				TransformGroup tg_root_s = (TransformGroup) branchGroup_static.getChild(0);
				TransformGroup tgs_staticobj = (TransformGroup) tg_root_s.getChild(0);
				tgs_staticobj.removeChild(obj_s);
				simpleUniverse.addBranchGraph(branchGroup_static);

				list_tg_obj_s_id.remove(in_dex);
				list_tg_obj_s.remove(in_dex);
				
			} else if (list_tg_obj_d_id.indexOf(oid) != -1) {//////////////////////////修改
				int in_dex = list_tg_obj_d_id.indexOf(oid);
				TransformGroup obj_d = (TransformGroup) list_tg_obj_d.get(in_dex);
				branchGroup_dynamic.detach();
				TransformGroup tg_root_d = (TransformGroup) branchGroup_dynamic.getChild(0);
				tg_root_d.removeChild(obj_d);
				simpleUniverse.addBranchGraph(branchGroup_dynamic);
				
				list_tg_obj_d_id.remove(in_dex);
				list_tg_obj_d.remove(in_dex);
				vectors_DynamicAttributes.remove(in_dex);
				
				//remove dynamic attribute
				vectors_mobjects.remove(in_dex);

				if (list_tg_obj_d.size() == 0) {
					animation = false;
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					TimeSlider.setValue(0);
					TimeBounds=null;
					setActualTime(TimeBounds);
				}
			}

			JComboBox CB = resultPanel.getQueryCombo();
			MObjQueryResult qr = (MObjQueryResult) CB.getItemAt(index);
			qr.clearSelection();

			// remove from ComboBox
			CB.removeItemAt(index);
			
			if (CB.getItemCount() == 0) {
				CB.setSelectedIndex(-1);
				resultPanel.clearComboBox();
				min_max_init = false;
			} else {
				CB.setSelectedIndex(0);
			}
			if (CB.getItemCount() > 0){
				CurrentResult = (MObjQueryResult) CB.getSelectedItem();
			}
		}
	}
	private int getQueryIndex(SecondoObject o) {
		JComboBox CB = resultPanel.getQueryCombo();
		int count = CB.getItemCount();
		int pos = -1;
		boolean found = false;
		ID id = o.getID();
		for (int i = 0; i < count && !found; i++)
			if (((MObjQueryResult) (CB.getItemAt(i))).hasId(id)) {
				pos = i;
				found = true;
			}
		return pos;
	}
	public void removeAll() {
//		System.out.println("removeAll");
		resultPanel.clearComboBox();
		TimeBounds = null;
//		SecondoObjects.removeAllElements();
//		if (VC != null)
//			VC.removeObject(null);
		showObject();
	}
	public boolean selectObject(SecondoObject O) {
//		System.err.println("--------------------------------selectObject");
		int i = SecondoObjects.indexOf(O);
		if (i >= 0) {
//			showObject();
			return true;
		} else // object not found
			return false;
	}
	private void showObject() {
//		System.err.println("--------------------------------showObject()");
//		String Text = "";
//		int index = -1;
//		if (index >= 0) {
//			
//		} else {
//			// set an empty text
//		}
	}
	public boolean addObject(SecondoObject o) {
//		System.err.println("--------addObject");
		if (!canDisplay(o)) return false;
		if (isDisplayed(o)) selectObject(o);
		else {
			ListExpr first = o.toListExpr().first();
			ListExpr second = o.toListExpr().second();
			
//////////////////////添加对象v02//****************//////////////
			if(first.isAtom()){
				JOptionPane.showMessageDialog(this, "This is not a visual objects");
			}else{
////////////////////////////////////////////////////////////////////////////////////////////////////
				if(first.first().symbolValue().equals("rtree3")){
//					System.out.println(">>>     This is init rtree");
					if (!min_max_init) {
						min = new Point3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
						max = new Point3d(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
					}
					if (!GetRange(o.toListExpr(), min, max))
						return false;
					min_max_init = true;
					TimeBounds = null;
					
					RTree_Info = o;
					
					int id = o.getID().hashCode() - 1;
					MObjQueryResult mqr = new MObjQueryResult(o);
					CurrentResult = mqr;
					resultPanel.resultToPanel_RTree(RTree_Info, CurrentResult);
				}
////////////////////////////////////////////////////////////////////////////////////////////////////  
				//1-RTree index; 2-mobjects; 3-trajectorys
				else if(first.second().second().listLength() == 8 && 
						first.second().second().third().second().symbolValue().equals("rect3")){
//					System.out.println(">>>   This is a 3d rect3 - rtree");
					if (min_max_init) {
						int id = o.getID().hashCode() - 1;
						TransformGroup stg_rtree = create3DRTree(second, id);
						MObjQueryResult mqr = new MObjQueryResult(o, stg_rtree, 1);
						CurrentResult = mqr;
						resultPanel.resultToPanel_RTree(RTree_Info, CurrentResult);
						                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
						branchGroup_static.detach();
						TransformGroup stg = (TransformGroup) branchGroup_static.getChild(0);
						TransformGroup stg_object = (TransformGroup) stg.getChild(0);
						stg_object.addChild(stg_rtree);
						simpleUniverse.addBranchGraph(branchGroup_static);
						
						list_tg_obj_s_id.add(id);
						list_tg_obj_s.add(stg_rtree);
					} else {
						JOptionPane.showMessageDialog(this, "3D bounding box is not initialized. First query Rtree !");
					}
				}
				else if(first.second().second().listLength() >= 2){//不确定格式对像
					ListExpr headers = first.second().second();
					while(!headers.isEmpty()){
						ListExpr header = headers.first();
						headers = headers.rest();
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
						if(header.second().symbolValue().equals("mpoint")){
//							System.out.println(">>>   This has a mpoint - moving object");
							if (min_max_init) {
								int id = o.getID().hashCode()-1;
								TransformGroup tgobj_mobject = createMObject(o.toListExpr(), id);
								MObjQueryResult mqr = new MObjQueryResult(o, tgobj_mobject, 2);
								CurrentResult = mqr;
							
								resultPanel.resultToPanel_MObjs(o, CurrentResult);
								
								vectors_DynamicAttributes.add(mqr.getMObjectsDattrs());
								
								branchGroup_dynamic.detach();
								TransformGroup dtg = (TransformGroup) branchGroup_dynamic.getChild(0);
								dtg.addChild(tgobj_mobject);
								simpleUniverse.addBranchGraph(branchGroup_dynamic);
								
								setActualTime(TimeBounds);
								animation = false;
								if (timer != null) {
									timer.cancel();
									timer = null;
								}
								
								list_tg_obj_d_id.add(id);
								list_tg_obj_d.add(tgobj_mobject);
							} else {
								JOptionPane.showMessageDialog(this, "3D bounding box is not initialized. First query Rtree !");
							}
							
							break;
						}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(header.second().symbolValue().equals("upoint")){
//							System.out.println(">>>   This has a upoint - trajetctory");
							
							if (min_max_init) {
								int id = o.getID().hashCode()-1;
								TransformGroup tgobj_trajectory = create3DTrajectory(second, id);
								MObjQueryResult mqr = new MObjQueryResult(o, tgobj_trajectory, 3);
								CurrentResult = mqr;
								resultPanel.resultToPanel_Trajs(o, CurrentResult);
								
								branchGroup_static.detach();
								TransformGroup stg = (TransformGroup) branchGroup_static.getChild(0);
								TransformGroup stg_object = (TransformGroup) stg.getChild(0);
								stg_object.addChild(tgobj_trajectory);
								simpleUniverse.addBranchGraph(branchGroup_static);
								
								list_tg_obj_s_id.add(id);
								list_tg_obj_s.add(tgobj_trajectory);
							} else {
								JOptionPane.showMessageDialog(this, "3D bounding box is not initialized. First query Rtree !");
							}
							
							break;
						}
					}
				}
				SecondoObjects.add(o);
			}
		}
		return true;
	}
	
	public boolean GetRange(ListExpr listexpr, Point3d min, Point3d max) {
		boolean result = true;
		if (listexpr.listLength() != 2)
			return false;
		else {
			ListExpr type = listexpr.first();
			ListExpr value = listexpr.second();
			
			if(listexpr.first().first().symbolValue().equals("rtree3")){
				ListExpr box = value.fifth().second();
				double sx,ex,sy,ey,st,et;
				sx = box.first().realValue();
				ex = box.second().realValue();
				sy = box.third().realValue();
				ey = box.fourth().realValue();
				st = box.fifth().realValue() * 86400.0;
				et = box.sixth().realValue() * 86400.0;
				min.x = Math.min(sx, min.x);
				min.y = Math.min(sy, min.y);
				min.z = Math.min(st, min.z);
				max.x = Math.max(ex, max.x);
				max.y = Math.max(ey, max.y);
				max.z = Math.max(et, max.z);
			}else{
				// analyze type
				ListExpr maintype = type.first();
				if (type.listLength() != 2 || !maintype.isAtom() || maintype.atomType() != ListExpr.SYMBOL_ATOM
						|| !(maintype.symbolValue().equals("rel") || maintype.symbolValue().equals("mrel")
								|| maintype.symbolValue().equals("trel")))
					return false; // not a relation
				ListExpr tupletype = type.second();

				// analyze Tuple
				ListExpr TupleFirst = tupletype.first();
				if (tupletype.listLength() != 2 || !TupleFirst.isAtom() || TupleFirst.atomType() != ListExpr.SYMBOL_ATOM
						|| !(TupleFirst.symbolValue().equals("tuple") | TupleFirst.symbolValue().equals("mtuple")))
					return false; // not a tuple

				ListExpr TupleTypeValue = tupletype.second();
				// the table head
				String[] head = new String[TupleTypeValue.listLength()];
				for (int i = 0; !TupleTypeValue.isEmpty() && result; i++) {
					ListExpr TupleSubType = TupleTypeValue.first();
					if (TupleSubType.listLength() != 2)
						result = false;
					else {
						// name of the attribute
						// head[i] = TupleSubType.first().writeListExprToString();
						// remove comment below if Type is wanted
						head[i] = TupleSubType.second().writeListExprToString();
						// System.out.println(head[i]);
					}
					TupleTypeValue = TupleTypeValue.rest();
				}
				
				if (result) {
					// analyze the values
					ListExpr TupleValue;
					Vector<String[]> V = new Vector<String[]>();
					String[] row;
					int pos;
					ListExpr Elem;
					while (!value.isEmpty()) { // for each tuple, analyze the data type
						TupleValue = value.first();
						row = new String[head.length];
						pos = 0;
						while (pos < head.length & !TupleValue.isEmpty()) {
							Elem = TupleValue.first();
							if (head[pos].equals("\nrect3")) {
								RangeMBR(listexpr.second(), min, max);
							}
							if (head[pos].equals("\nmpoint")) {
								RangeMPoint(Elem, min, max);
							}
							if (head[pos].equals("\nupoint")) {
								RangeUpoint(listexpr.second(), min, max);
							}
							
							row[pos] = Elem.writeListExprToString();
							pos++;
							TupleValue = TupleValue.rest();
						}
						V.add(row);
						value = value.rest();
					}
				}
			}
		}
		return true;
	}
	public void RangeUpoint(ListExpr listexpr, Point3d min, Point3d max){
		while (!listexpr.isEmpty()) {
			ListExpr LE = listexpr.first().second();
			ListExpr timebounds = LE.first();
			ListExpr loc1 = LE.second();
			listexpr = listexpr.rest();
			/////////////////// read time information//////////////////////////
			double st, et;
			boolean lt, rt;
			lt = timebounds.third().boolValue();
			rt = timebounds.fourth().boolValue();
			st = LEUtils.readInstant(timebounds.first());
			et = LEUtils.readInstant(timebounds.second());
			st = st * 86400.0;
			et = et * 86400.0;
			double coord_x1 = loc1.first().realValue();
			double coord_y1 = loc1.second().realValue();
			double coord_x2 = loc1.third().realValue();
			double coord_y2 = loc1.fourth().realValue();
			min.x = Math.min(coord_x1, min.x);
			min.y = Math.min(coord_y1, min.y);
			min.z = Math.min(st, min.z);
			max.x = Math.max(coord_x2, max.x);
			max.y = Math.max(coord_y2, max.y);
			max.z = Math.max(et, max.z);
		}
	}
	public void RangeMBR(ListExpr listexpr, Point3d min, Point3d max) {
		while (!listexpr.isEmpty()) {
			ListExpr LE = listexpr.first();
			listexpr = listexpr.rest();
			
			double x1 = LE.first().first().realValue();
			double x2 = LE.first().second().realValue();
			double y1 = LE.first().third().realValue();
			double y2 = LE.first().fourth().realValue();
			double z1 = LE.first().fifth().realValue() * 86400.0;
			double z2 = LE.first().sixth().realValue() * 86400.0;
			
			min.x = Math.min(x1, min.x);
			min.y = Math.min(y1, min.y);
			min.z = Math.min(z1, min.z);
			max.x = Math.max(x2, max.x);
			max.y = Math.max(y2, max.y);
			max.z = Math.max(z2, max.z);
		}
	}
	public void RangeMPoint(ListExpr listexpr, Point3d min, Point3d max) {
		while (!listexpr.isEmpty()) {
			ListExpr LE = listexpr.first();
			ListExpr timebounds = LE.first();
			ListExpr loc1 = LE.second();
			listexpr = listexpr.rest();
			/////////////////// read time information//////////////////////////
			double st, et;
			boolean lt, rt;
			lt = timebounds.third().boolValue();
			rt = timebounds.fourth().boolValue();
			st = LEUtils.readInstant(timebounds.first());
			et = LEUtils.readInstant(timebounds.second());
			st = st * 86400.0;
			et = et * 86400.0;
			double coord_x1 = loc1.first().realValue();
			double coord_y1 = loc1.second().realValue();
			double coord_x2 = loc1.third().realValue();
			double coord_y2 = loc1.fourth().realValue();
			min.x = Math.min(coord_x1, min.x);
			min.y = Math.min(coord_y1, min.y);
			min.z = Math.min(st, min.z);
			max.x = Math.max(coord_x2, max.x);
			max.y = Math.max(coord_y2, max.y);
			max.z = Math.max(et, max.z);
		}
	}
	public void MapFunction(Point3d p, Point3d min, Point3d max) {
		Double minx = new Double(min.x);
		Double miny = new Double(min.y);
		Double minz = new Double(min.z);
		Double maxx = new Double(max.x);
		Double maxy = new Double(max.y);
		Double maxz = new Double(max.z);
		
		if (maxx.compareTo(minx) > 0) {
			double range = max.x - min.x;
			p.x = 2 * (p.x - min.x) / range - 1.0;
		} 
		else p.x = 0.0f;

		if (maxy.compareTo(miny) > 0) {
			double range = max.y - min.y;
			p.y = 2 * (p.y - min.y) / range - 1.0;
		} 
		else p.y = 0.0f;

		if (maxz.compareTo(minz) > 0) {
			double range = max.z - min.z;
			p.z = 2 * (p.z - min.z) / range - 1.0;
		} 
		else p.z = 0.0f;
	}
	public void getMovingObject(ListExpr LE){
		ListExpr LE_mo = LE;
		
		positions_start = new Vector3d[LE_mo.listLength()];
		positions_end = new Vector3d[LE_mo.listLength()];
		timeBounds = new Vector<Interval>(0);
		
		int count = 0;
		ListExpr upoint_list = LE_mo;
		
		while (!upoint_list.isEmpty()) {
			ListExpr P = upoint_list.first();
			upoint_list = upoint_list.rest();

			ListExpr timebound = P.first();
			ListExpr location = P.second();

			double st, et;
			boolean lt, rt;
			lt = timebound.third().boolValue();
			rt = timebound.fourth().boolValue();
			st = LEUtils.readInstant(timebound.first());
			et = LEUtils.readInstant(timebound.second());
			timeBounds.add(new Interval(st, et, lt, rt));
			if (TimeBounds == null)
				TimeBounds = new Interval(st, et, lt, rt);
			else
				TimeBounds.unionInternal(new Interval(st, et, lt, rt));

			st = st * 86400.0;
			et = et * 86400.0;
			double x1 = location.first().realValue();
			double y1 = location.second().realValue();
			double x2 = location.third().realValue();
			double y2 = location.fourth().realValue();
			
			Point3d p1 = new Point3d(x1, y1, st);
			MapFunction(p1, min, max);
			vertices.add(new Point3d(p1.x, p1.y, p1.z));
			positions_start[count] = new Vector3d(p1.x, p1.y, p1.z);

			Point3d p2 = new Point3d(x2, y2, et);
			MapFunction(p2, min, max);
			vertices.add(new Point3d(p2.x, p2.y, p2.z));
			positions_end[count] = new Vector3d(p2.x, p2.y, p2.z);
			
			count++;
		} 	
	}
	public TransformGroup create3DRTree(ListExpr LE, int id){
		TransformGroup objTran = new TransformGroup();
		objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objTran.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		objTran.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		objTran.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
		
		ListExpr le_rtree = LE;
		
		while(!le_rtree.isEmpty()){
			ListExpr node = le_rtree.first();
			le_rtree = le_rtree.rest();
			
			//3D　R-Tree 线条样式
			Appearance app = new Appearance();
			LineAttributes line_attr = new LineAttributes();
			//0-实线， 1-虚线, 2-点线， 3-点画线
	        line_attr.setLinePattern(0);
	        line_attr.setLineAntialiasingEnable(false);
	        app.setLineAttributes(line_attr);
	        
			ColoringAttributes color_attr = new ColoringAttributes();
	        color_attr.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
	        
	        if(Integer.valueOf(node.first().writeListExprToString().trim())
	        		> Integer.valueOf(RTree_Info.toListExpr().second().second().second().writeListExprToString().trim())){
				color_attr.setColor(COLOR_WRITE);
			}else if(node.fifth().writeListExprToString().equals("\nFALSE")){
				color_attr.setColor(COLOR_YELLOW);
			}else{
				color_attr.setColor(COLOR_RED);
			}
			app.setColoringAttributes(color_attr);
			
			double x1 = node.third().first().realValue();
			double x2 = node.third().second().realValue();
			double y1 = node.third().third().realValue();
			double y2 = node.third().fourth().realValue();
			double z1 = node.third().fifth().realValue() * 86400.0;
			double z2 = node.third().sixth().realValue() * 86400.0;
			
			Point3d p1 = new Point3d(x1, y1, z1);
			Point3d p2 = new Point3d(x2, y2, z2);
			
			MapFunction(p1, min, max);
			MapFunction(p2, min, max);
			
			Point3d[] mbr3 = new Point3d[24];
			mbr3[0] = new Point3d(p1.x, p1.y, p1.z);
			mbr3[1] = new Point3d(p2.x, p1.y, p1.z);
			mbr3[2] = new Point3d(p2.x, p1.y, p1.z);
			mbr3[3] = new Point3d(p2.x, p2.y, p1.z);
			mbr3[4] = new Point3d(p2.x, p2.y, p1.z);
			mbr3[5] = new Point3d(p1.x, p2.y, p1.z);
			mbr3[6] = new Point3d(p1.x, p2.y, p1.z);
			mbr3[7] = new Point3d(p1.x, p1.y, p1.z);
			mbr3[8] = new Point3d(p1.x, p1.y, p1.z);
			mbr3[9] = new Point3d(p1.x, p1.y, p2.z);
			mbr3[10] = new Point3d(p2.x, p1.y, p1.z);
			mbr3[11] = new Point3d(p2.x, p1.y, p2.z);
			mbr3[12] = new Point3d(p2.x, p2.y, p1.z);
			mbr3[13] = new Point3d(p2.x, p2.y, p2.z);
			mbr3[14] = new Point3d(p1.x, p2.y, p1.z);
			mbr3[15] = new Point3d(p1.x, p2.y, p2.z);
			mbr3[16] = new Point3d(p1.x, p1.y, p2.z);
			mbr3[17] = new Point3d(p2.x, p1.y, p2.z);
			mbr3[18] = new Point3d(p2.x, p1.y, p2.z);
			mbr3[19] = new Point3d(p2.x, p2.y, p2.z);
			mbr3[20] = new Point3d(p2.x, p2.y, p2.z);
			mbr3[21] = new Point3d(p1.x, p2.y, p2.z);
			mbr3[22] = new Point3d(p1.x, p2.y, p2.z);
			mbr3[23] = new Point3d(p1.x, p1.y, p2.z);

	        LineArray line = new LineArray(24, GeometryArray.COORDINATES);
	        line.setCoordinates(0, mbr3);
	        line.setCapability(Geometry.ALLOW_INTERSECT);
	        Shape3D mbr3D = new Shape3D(line,app);
	        objTran.addChild(mbr3D);
		}
		objTran.setName("rtree"+id);
		return objTran;
	}
	public TransformGroup create3DTrajectory(ListExpr LE, int id){
		TransformGroup objTran = new TransformGroup();
		objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objTran.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		objTran.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		objTran.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
		ListExpr point_list = LE;
		
		Vector<Point3d> upoints = new Vector<Point3d>(0);
		
		//轨迹线条样式
		Appearance myAppear = new Appearance();
		ColoringAttributes myColorAttr = new ColoringAttributes();
		myColorAttr.setColor(1.0f, 1.0f, 1.0f); 
        myColorAttr.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
        myAppear.setColoringAttributes(myColorAttr);
        LineAttributes myLineAttr = new LineAttributes();
        //0-实线， 1-虚线, 2-点线， 3-点画线
        myLineAttr.setLinePattern(0);
        myLineAttr.setLineAntialiasingEnable(false);
        myAppear.setLineAttributes(myLineAttr);
		
		while(!point_list.isEmpty()){
			ListExpr point = point_list.first();
			point_list = point_list.rest();
			
			ListExpr timebounds = point.second().first();
			ListExpr loc = point.second().second();
			double st, et;
			boolean lt, rt;
			lt = timebounds.third().boolValue();
			rt = timebounds.fourth().boolValue();
			st = LEUtils.readInstant(timebounds.first());
			et = LEUtils.readInstant(timebounds.second());
			st = st * 86400.0;
			et = et * 86400.0;

			double coord_x1 = loc.first().realValue();
			double coord_y1 = loc.second().realValue();
			double coord_x2 = loc.third().realValue();
			double coord_y2 = loc.fourth().realValue();
			
			Point3d p1 = new Point3d(coord_x1, coord_y1, st);
			MapFunction(p1, min, max);
			Point3d p2 = new Point3d(coord_x2, coord_y2, et);
			MapFunction(p2, min, max);
			
			upoints.add(p1);
			upoints.add(p2);
		}
		
		for(int i=0;i<upoints.size();){
			Point3d p1 = upoints.get(i++);
			Point3d p2 = upoints.get(i++);
			
			Point3d[] myupoint = new Point3d[2];
			myupoint[0] = new Point3d(p1.x, p1.y, p1.z);
			myupoint[1] = new Point3d(p2.x, p2.y, p2.z);
			
	        LineArray mytrips = new LineArray(myupoint.length, GeometryArray.COORDINATES);
	        mytrips.setCoordinates(0, myupoint);
	        mytrips.setCapability(Geometry.ALLOW_INTERSECT);
	        Shape3D trip3D = new Shape3D(mytrips,myAppear);
			
	        objTran.addChild(trip3D);
		}
		objTran.setName("traj"+id);
		return objTran;
	}
	public TransformGroup createMObject(ListExpr LE, int id){
		TransformGroup dtg_obj = new TransformGroup();
		dtg_obj.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		dtg_obj.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		dtg_obj.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		dtg_obj.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		
		Vector vector_mobject = new Vector();
		
		ListExpr tuple_headers = LE.first().second().second();
		ListExpr tuples = LE.second();
		
		int ct_index = 0;
		while(!tuples.isEmpty()){
			ListExpr tuple = tuples.first();
			tuples = tuples.rest();
			ListExpr headers = tuple_headers;
			while(!headers.isEmpty()){
				ListExpr header = headers.first();
				headers = headers.rest();
				ListExpr content = tuple.first();
				tuple = tuple.rest();
				
				if(header.second().symbolValue().equals("mpoint")){
					getMovingObject(content);
					MObject mobject = new MObject(ct_index++, positions_start, positions_end, timeBounds);
					vector_mobject.add(mobject);
					
					
					TransformGroup objTran = new TransformGroup();
					objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
					objTran.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
					objTran.setCapability(Group.ALLOW_CHILDREN_READ);
					objTran.setCapability(Group.ALLOW_CHILDREN_WRITE);
					
					Transform3D trans3d = mobject.getStartTrans3D();
					objTran.setTransform(trans3d);
					objTran.addChild(mobject.CreateObj());
					dtg_obj.addChild(objTran);
				}
			}
		}
		vectors_mobjects.add(vector_mobject);
		dtg_obj.setName("mobj"+id);
		return dtg_obj;
	}
	
	public void setActualTime(Interval in) {
		TimeBounds = in;
		if (in == null) {
			TimeSlider.setValues(0, 0, 1);
			actTimeLabel.setText("NO TIME");
			CurrentState.ActualTime = 0;
		} else {
			TimeBounds.increase(TIMEBORDER);
			TimeSlider.setVisible(true);
			CurrentState.ActualTime = TimeBounds.getStart();
			TimeSlider.setValues((long) Math.round(in.getStart() * 86400000),
								 (long) Math.round(in.getStart() * 86400000), 
								 (long) Math.round(in.getEnd() * 86400000) + 1);
			actTimeLabel.setText(LEUtils.convertTimeToString(CurrentState.ActualTime));
			ani_ct = TimeBounds.getStart();
		}
	}
	public void mouseClicked(MouseEvent e) {
		pickCanvas.setShapeLocation(e);
		PickResult result = pickCanvas.pickClosest();
		if (result == null) {
			
		} else {
			int index = Integer.valueOf(result.getObject().getName());
			int ct_index = index * (CurrentResult.model_size + 2);
//			System.out.println(">>>>>> Moving Object "+ct_index);
			CurrentResult.setSelectedIndex(ct_index);
			CurrentResult.ensureIndexIsVisible(ct_index);
		}
	}
	public void mousePressed(MouseEvent e) {
		
	}
	public void mouseReleased(MouseEvent e) {
		
	}
	public void mouseEntered(MouseEvent e) {
		
	}
	public void mouseExited(MouseEvent e) {
		
	}

	void setScale(float s) {
		//////////////// change the object/////////////////////////////
		transformGroup_static.getTransform(tran); // get the old transform
		tran.setScale(s); // set only scale
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.setScale(s); // set only scale
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}
	void ResetRotation() {
		transformGroup_static.getTransform(tran); // get the old transform
		tran.rotX(Math.toRadians(0));
		tran.rotY(Math.toRadians(0));
		tran.rotZ(Math.toRadians(0));
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.rotX(Math.toRadians(0));
		tran.rotY(Math.toRadians(0));
		tran.rotZ(Math.toRadians(0));
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}
	void setViewX(double angle) {
		transformGroup_static.getTransform(tran); // get the old transform
		tran.rotX(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.rotX(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}	
	void setViewY(double angle) {
		transformGroup_static.getTransform(tran); // get the old transform
		tran.rotY(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.rotY(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}
	void setViewZ(double angle) {
		transformGroup_static.getTransform(tran); // get the old transform
		tran.rotZ(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.rotZ(Math.toRadians(angle));
		tran.setScale(objScale);
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}
	void setTransloation() {
		transformGroup_static.getTransform(tran); // get the old transform
		tran.setTranslation(VecTranslation); // set only scale
		transformGroup_static.setTransform(tran); // set the new transform
		
		transformGroup_dynamic.getTransform(tran); // get the old transform
		tran.setTranslation(VecTranslation); // set only scale
		transformGroup_dynamic.setTransform(tran); // set the new transform
	}
	
	public synchronized void updateMObjsCoord(double ct) throws Exception{
		TransformGroup TG_dynamic = (TransformGroup) branchGroup_dynamic.getChild(0);
		for(int i=0; i<TG_dynamic.numChildren(); i++){
			TransformGroup dtg_obj = (TransformGroup) TG_dynamic.getChild(i);
			Vector vec_mo = (Vector) vectors_mobjects.get(i);
			for(int j=0;j<dtg_obj.numChildren();j++){
				TransformGroup objTran = (TransformGroup) dtg_obj.getChild(j);
				MObject ball = (MObject) vec_mo.get(j);
	
				if (ball.getStart() > ct) {
					if (objTran.numChildren() > 0){
						Vector3d v3d = new Vector3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
						Transform3D trans3d = new Transform3D();
						trans3d.set(v3d);
						objTran.setTransform(trans3d);
					}
					continue;
				}
				if (ball.getEnd() < ct){
					if (objTran.numChildren() > 0){
						Vector3d v3d = new Vector3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
						Transform3D trans3d = new Transform3D();
						trans3d.set(v3d);
						objTran.setTransform(trans3d);
					}
					continue;
				}
				
				Transform3D trans3d = ball.UpdateLoc(ct);
				if(trans3d != null){
					objTran.setTransform(trans3d);
				}
			}
		}
	}	
	public synchronized void updateDattribute(double ct) throws Exception{
//		//current object
		for(int i=0;i<CurrentResult.getTupleCount();i++){
			Vector mobject_dattrs = (Vector)CurrentResult.getMObjectsDattrs().get(i);
			for(int j=0;j<mobject_dattrs.size();j++){
				Vector name_dattr = (Vector)mobject_dattrs.get(j);
				String temp_name = name_dattr.get(0).toString();
				DynamicAttribute temp_dattr = (DynamicAttribute)name_dattr.get(1);
				
				int index = temp_dattr.updateDattr(ct);
				if(index>=0 && index!=temp_dattr.getCti()){
					listModel = (DefaultListModel) (CurrentResult.getModel());
					for(int h=i*(CurrentResult.model_size+2);h<(i+1)*(CurrentResult.model_size+2)-2;h++){
						if(listModel.get(h).equals(temp_name+" : ")){
							String str = "        [ "+temp_dattr.getAttributes().get(index).toString()+" ]";
							listModel.set(++h, str);
						}
					}
				}
			}
		}
		
//		all objects
		for(int i=0; i<vectors_DynamicAttributes.size() && i<vectors_mobjects.size(); i++){
			Vector vector_dattrs = (Vector) vectors_DynamicAttributes.get(i);
			Vector vector_mobjects = (Vector) vectors_mobjects.get(i);
			for(int j=0; j<vector_dattrs.size(); j++){
				Vector mobject_dattrs = (Vector) vector_dattrs.get(j);
				MObject mobject = (MObject) vector_mobjects.get(j);
				for(int k=0;k<mobject_dattrs.size();k++){
					Vector mobj_dattr = (Vector) mobject_dattrs.get(k);
					for(int h=0;h<mobj_dattr.size();h++){
						h++;
						DynamicAttribute temp_dattr = (DynamicAttribute)mobj_dattr.get(h);
						
						int index = temp_dattr.updateDattr(ct);
						if(index>=0 && temp_dattr.getCti()!=index){
							temp_dattr.setCti(index);
							
							TransformGroup TG_dynamic = (TransformGroup)branchGroup_dynamic.getChild(0);
							TransformGroup dtg_obj = (TransformGroup)TG_dynamic.getChild(i);
							TransformGroup objTran = (TransformGroup)dtg_obj.getChild(j);
							Shape3D son = (Shape3D)objTran.getChild(0);
							
							//mbool类型变颜色
							if(temp_dattr.getAttributes().get(index).toString().equals("FALSE")){
								((PointArray)son.getGeometry()).setColor(0, COLOR_GREEN);
							}
							if(temp_dattr.getAttributes().get(index).toString().equals("TRUE")){
								((PointArray)son.getGeometry()).setColor(0, COLOR_RED);
							}
						}
					}
				}
			}
		}
	}
	
	class AnimCtrlListener implements ActionListener, Runnable{
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			switch (Integer.parseInt(evt.getActionCommand())) {
			case 0: // play
//				System.out.println("play");
				delay_val = 20;
				if (animation == false) {
					if (timer == null) {
						if (TimeBounds != null) {
							timer = new Timer();
							ani_ct = TimeBounds.getStart();
							timer.schedule(new AniMObj(), 0, 50);
						}
					}
					animation = true;
				}
				break;
			case 1: // reverse
//				System.out.println("reverse");
				delay_val = -20;
				break;
			case 2: // to end
//				System.out.println("ToEnd");
				TimeSlider.setValue(TimeSlider.getMaximum());
				ani_ct = TimeBounds.getEnd();
				animation = false;
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				break; 
			case 3: // to start
//				System.out.println("ToStart");
				TimeSlider.setValue(TimeSlider.getMinimum());
				ani_ct = TimeBounds.getStart();
				break;
			case 4: // stop
//				System.out.println("Stop");
				animation = false;
				break;
			case 5: //decrease speed
//				System.out.println("Decrease Speed");
				delay_val = delay_val / 2;
				if (Math.abs(delay_val) < 2) {
					if (delay_val > 0)
						delay_val = 2;
					else
						delay_val = -2;
				}
				break;
			case 6://increase speed
//				System.out.println("Increase Speed");
				delay_val = delay_val * 2; 
				break;
			}
		}

		public void run() {

		}
	}
	class AniMObj extends TimerTask {
		public void run() {
			long l = (TimeSlider.getMaximum() - TimeSlider.getMinimum());
			while (TimeBounds != null && ani_ct <= TimeBounds.getEnd() && animation) {
				double delta = 0.0;
				if (TimeBounds != null)
					delta = (ani_ct - TimeBounds.getStart()) / (TimeBounds.getEnd() - TimeBounds.getStart());
				long v = TimeSlider.getMinimum() + (new Double(delta * l)).longValue();
				TimeSlider.setValue(v);
				ani_ct += delay_val / 86400000.0;
				
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (TimeSlider.getValue() != v) {
					ani_ct = TimeSlider.getValue() / 86400000.0;
				}
				
			}
			if (TimeBounds != null && ani_ct > TimeBounds.getEnd()) {
				animation = false;
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			}
		}
	}
	class TimeChangeListener implements ChangeValueListener {
		public void valueChanged(ChangeValueEvent e) {
			if (TimeBounds == null) {
				TimeSlider.setValue(0);
				return;
			}
			long v = e.getValue();
			double anf;
			if (v == TimeSlider.getMinimum())
				anf = TimeBounds.getStart();
			else {
				anf = (double) v / 86400000.0;
				if (anf > TimeBounds.getEnd())
					anf = TimeBounds.getEnd();
			}
			if (anf == CurrentState.ActualTime) {
				return;
			}
			CurrentState.ActualTime = anf;
			actTimeLabel.setText(LEUtils.convertTimeToString(CurrentState.ActualTime));
			
			try {
				updateMObjsCoord(CurrentState.ActualTime);
				updateDattribute(CurrentState.ActualTime);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	class ViewCtrlListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == Resetbtn) {
				ResetRotation();
				setTransloation();
				setScale(objScale);
			} else if (e.getSource() == RVbtn) {
				setViewY(-90);
			} else if (e.getSource() == BVbtn) {
				setViewY(180);
			} else if (e.getSource() == TVbtn) {
				setViewX(90);
			} else if (e.getSource() == LVbtn) {
				setViewY(90);
			}  
		}
	}

}