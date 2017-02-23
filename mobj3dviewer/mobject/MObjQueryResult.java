//This file is part of SECONDO.

//Copyright (C) 2004, University in Hagen, Department of Computer Science, 
//Database Systems for New Applications.

//SECONDO is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//SECONDO is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with SECONDO; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package viewer.mobject;

import sj.lang.ListExpr;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import viewer.hoese.*;
import gui.idmanager.ID;
import gui.SecondoObject;

import javax.media.j3d.*;

public class MObjQueryResult extends JList {
	
	private ID id;
	public String command;
	public ListExpr LEResult;
	private int TupelCount, AttrCount;        
	private String[] attr_name, attr_type;
	public TransformGroup tg_obj;
	private Interval interval;
	private boolean rel_obj;
	public int type;
	
	//Dynamic Atrributes
	public Vector mobjects_dattrs = new Vector(0); 
	public int dattr_count=0;
	
	//model_size
	public int model_size;
	
	public MObjQueryResult(SecondoObject so) {
		super();
		String acommand = so.getName();
		ListExpr aLEResult = so.toListExpr();
		
		this.id = so.getID();
		this.command = acommand;
		this.LEResult = aLEResult;
		this.rel_obj = true;
		this.tg_obj = null;
		this.interval = null;
		this.type = -1;
		
//		this.addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent e) {
//				System.out.println(">>> : "+acommand);
//			}
//		});
		
		this.setFont(new Font("Monospaced", Font.PLAIN, 14));
		this.setModel(new DefaultListModel());
		this.setCellRenderer(new QueryRenderer());
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setBackground(Color.lightGray);
		
	}
	
	public MObjQueryResult(SecondoObject so, final TransformGroup tg, int type) {
		super();
		String acommand = so.getName();
		ListExpr aLEResult = so.toListExpr();
		
		this.id = so.getID();
		this.command = acommand;
		this.LEResult = aLEResult;
		this.rel_obj = Get_Attr(aLEResult);
		this.tg_obj = tg;
		this.type = type;

		if (rel_obj) {
			this.TupelCount = LEResult.second().listLength();
		} else
			this.TupelCount = 0;
		if (LEResult.first().isAtom()) {
			this.AttrCount = 0;
		} else {
			try {
				this.AttrCount = LEResult.first().second().second().listLength();
			} catch (Exception e) {
				this.AttrCount = 0;
			}
		}
		
		if(type == 2){//mobjects
			//移动对象的动态属性
			ListExpr tuple_headers = aLEResult.first().second().second();
			ListExpr tuples = aLEResult.second();
			
			ListExpr headers_temp = tuple_headers;
			while(!headers_temp.isEmpty()){
				ListExpr header = headers_temp.first();
				headers_temp = headers_temp.rest();
				
				if(header.second().symbolValue().equals("mstring") 
						|| header.second().symbolValue().equals("mreal") 
						|| header.second().symbolValue().equals("mint") 
						|| header.second().symbolValue().equals("mbool")){
					this.dattr_count++;
				}
			}
			
			while(!tuples.isEmpty()){
				ListExpr headers = tuple_headers;
				ListExpr tuple = tuples.first();
				tuples = tuples.rest();
				
				Vector mobject_dattrs = new Vector();
				while(!headers.isEmpty()){
					ListExpr header = headers.first();
					ListExpr content = tuple.first();
					headers = headers.rest();
					tuple = tuple.rest();
					
					if(header.second().symbolValue().equals("mstring") 
							|| header.second().symbolValue().equals("mreal") 
							|| header.second().symbolValue().equals("mint") 
							|| header.second().symbolValue().equals("mbool")){
						DynamicAttribute dattr = new DynamicAttribute(so.getID(), content);
						Vector name_dattr = new Vector();
						name_dattr.add(header.first().symbolValue());
						name_dattr.add(dattr);
						mobject_dattrs.add(name_dattr);
					}
				}
				mobjects_dattrs.add(mobject_dattrs);
			}
			this.model_size = dattr_count + AttrCount;
			
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
						int index = MObjQueryResult.this.locationToIndex(e.getPoint());
						int obj_index = (int) index / (model_size + 2);
						TransformGroup temp_child_tg = (TransformGroup) tg.getChild(obj_index);
						if(temp_child_tg.numChildren()!=0){
							Shape3D son = (Shape3D) temp_child_tg.getChild(0);
							
							Appearance appearence = new Appearance();
							appearence.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
							appearence.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
							appearence.setPointAttributes(new PointAttributes(15.0f, true));
							
							son.setAppearance(appearence);
						}
					}
					
					if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
						int index = MObjQueryResult.this.locationToIndex(e.getPoint());
						int obj_index = (int) index / (model_size + 2);
						TransformGroup temp_child_tg = (TransformGroup) tg.getChild(obj_index);
						if(temp_child_tg.numChildren()!=0){
							Shape3D son = (Shape3D) temp_child_tg.getChild(0);
							
							Appearance appearence = new Appearance();
							appearence.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
							appearence.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
							appearence.setPointAttributes(new PointAttributes(8.0f, true));
							
							son.setAppearance(appearence);
						}
					}
				}
			});
		}
		
		this.setFont(new Font("Monospaced", Font.PLAIN, 14));		
		this.setModel(new DefaultListModel());
		this.setCellRenderer(new QueryRenderer());
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setBackground(Color.lightGray);
	}
	
	public boolean Get_Attr(ListExpr LE) {
		ListExpr type = LE.first();
		ListExpr value = LE.second();

		if (type.isAtom()) return false;

		// analyze type
		ListExpr maintype = type.first();
		if (type.listLength() != 2 
				|| !maintype.isAtom() || maintype.atomType() != ListExpr.SYMBOL_ATOM
				|| !(maintype.symbolValue().equals("rel") || maintype.symbolValue().equals("mrel") || maintype.symbolValue().equals("trel")))
			return false; // not a relation
		
		// analyze tuple
		ListExpr tupletype = type.second();
		ListExpr TupleFirst = tupletype.first();
		if (tupletype.listLength() != 2 || !TupleFirst.isAtom() || TupleFirst.atomType() != ListExpr.SYMBOL_ATOM
				|| !(TupleFirst.symbolValue().equals("tuple") | TupleFirst.symbolValue().equals("mtuple")))
			return false; // not a tuple

		ListExpr TupleTypeValue = tupletype.second();
		attr_type = new String[TupleTypeValue.listLength()];
		attr_name = new String[TupleTypeValue.listLength()];
		for (int i = 0; !TupleTypeValue.isEmpty(); i++) {
			ListExpr TupleSubType = TupleTypeValue.first();
			if (TupleSubType.listLength() != 2)
				return false;
			else {
				attr_name[i] = TupleSubType.first().writeListExprToString();
				attr_type[i] = TupleSubType.second().writeListExprToString();
			}
			TupleTypeValue = TupleTypeValue.rest();
		}
		return true;
	}
	// change the color of selected item
	public void SetSelectItem(int index) {
		int pos;
		// System.out.println("select index " + index);
		if (rel_obj) {
			index = index * (AttrCount + 1);
			// System.out.println(AttrCount);
			for (int i = 0; i < AttrCount; i++) {
				// System.out.println("attr_name "+attr_name[i]);
			}
		} else {// only one object, not a relation
			setSelectedIndex(index);
			ensureIndexIsVisible(index);
		}
	}
	public ListExpr getPick() {
		if (AttrCount == 0) {
			return LEResult;
		}
		int selind = getSelectedIndex();
		int TupelNr = (selind / (AttrCount + 1));
		int AttrNr = (selind % (AttrCount + 1));
		if (AttrNr == AttrCount)
			return null; // Separator
		ListExpr TupelList = LEResult.second();
		for (int i = 0; i < TupelNr; i++)
			TupelList = TupelList.rest();
		ListExpr AttrList = TupelList.first();
		for (int i = 0; i < AttrNr; i++)
			AttrList = AttrList.rest();
		ListExpr TypeList = LEResult.first().second().second();
		for (int i = 0; i < AttrNr; i++)
			TypeList = TypeList.rest();
		return ListExpr.twoElemList(TypeList.first().second(), AttrList.first());
	}
	/**
	 * search the given String in this list and returns the index, the search is
	 * started with offset and go to the end of the list. if the given string is
	 * not containing between offset and end -1 is returned
	 */
	public int find(String S, boolean CaseSensitiv, int Offset) {
		ListModel LM = getModel();
		if (LM == null)
			return -1;
		String UCS = S.toUpperCase();
		boolean found = false;
		int pos = -1;
		for (int i = Offset; i < LM.getSize() && !found; i++) {
			if (CaseSensitiv && LM.getElementAt(i).toString().indexOf(S) >= 0) {
				pos = i;
				found = true;
			}
			if (!CaseSensitiv && LM.getElementAt(i).toString().toUpperCase().indexOf(UCS) >= 0) {
				pos = i;
				found = true;
			}
		}
		return pos;
	}
	public boolean equals(Object o) {
		if (!(o instanceof MObjQueryResult))
			return false;
		else {
			MObjQueryResult qr = (MObjQueryResult) o;
			return command.equals(qr.command) && (LEResult == (qr.LEResult));
		}
	}
	public void addEntry(Object entry) {
		if (entry != null) {
			if (entry instanceof DsplBase) {
				if (((DsplBase) entry).getFrame() != null) {
					((DsplBase) entry).getFrame().addObject(entry);
				}
			}
		}
		((DefaultListModel) getModel()).addElement(entry);
	}
	public void computeTimeBounds() {
		ListModel listModel = getModel();
		int size = listModel.getSize();
		this.interval = null;
		for (int i = 0; i < size; i++) {
			Object o = listModel.getElementAt(i);
			if (o instanceof Timed) {
				Interval oInterval = ((Timed) o).getBoundingInterval();
				if (oInterval != null) {
					if (this.interval == null) {
						this.interval = oInterval.copy();
					} else {
						this.interval.unionInternal(oInterval);
					}
				}
			}
		}
	}
	
	public ID getId() {
		return id;
	}
	public void setId(ID id) {
		this.id = id;
	}
	public boolean hasId(ID id) {
		return this.id.equals(id);
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public int getTupleCount() {
		return TupelCount;
	}
	public int getAttrCount(){
		return AttrCount;
	}
	public TransformGroup GetTG() {
		return tg_obj;
	}
	public ListExpr getListExpr() {
		return LEResult;
	}
	public Interval getBoundingInterval() {
		return interval;
	}
	public String toString() {
		return command;
	}
	public Vector getMObjectsDattrs() {
		return mobjects_dattrs;
	}
	public void setMOjectsDattrs(Vector mosdas) {
		mobjects_dattrs = mosdas;
	}

	// A class for special rendering of data types in the list
	private class QueryRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setForeground(Color.BLACK);
			if ((value instanceof DsplGraph) && (value instanceof Timed))
				setForeground(Color.magenta);
			else if (value instanceof DsplGraph)
				setForeground(Color.red);
			else if (value instanceof Timed)
				setForeground(Color.blue);
			else if (value instanceof DsplBase)
				setForeground(new Color(0, 100, 0));
			if (value instanceof DsplBase)
				if (!((DsplBase) value).getVisible())
					setForeground(Color.gray);
			if (value instanceof ExternDisplay)
				setForeground(new Color(255, 0, 0));
			return this;
		}
	}
	
}