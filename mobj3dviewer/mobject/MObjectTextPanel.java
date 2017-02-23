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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;
import sj.lang.ListExpr;
import sj.lang.ServerErrorCodes;
import tools.Reporter;
import gui.SecondoObject;
import viewer.MObjectViewer;
import viewer.hoese.QueryResult;

public class MObjectTextPanel extends JPanel {

	//north
	private static JComboBox QueryCombo;
	
	//center
	private JScrollPane QueryScrollPane = new JScrollPane();
	private JPanel dummy = new JPanel();
	
	//south
	private JTextField SearchText = new JTextField(6);
	private JButton SearchBtn = new JButton("go");
	
	private static final int NOT_ERROR_CODE = ServerErrorCodes.NOT_ERROR_CODE;
	
	public MObjectTextPanel(final MObjectViewer ap) {
		super();
		this.setLayout(new BorderLayout());
		this.QueryCombo = new JComboBox(new DefaultComboBoxModel());
		this.QueryCombo.setMaximumSize(new Dimension(200, 300));
		this.setMinimumSize(new Dimension(100, 100));
		this.add(QueryCombo, BorderLayout.NORTH);
		this.QueryCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MObjQueryResult qr = null;
				qr = (MObjQueryResult) QueryCombo.getSelectedItem();
				ap.CurrentResult = qr;
				if (qr != null) {
					qr.clearSelection();
					QueryScrollPane.setViewportView(qr);
				} else
					QueryScrollPane.setViewportView(dummy);
				
			}
		});
		
		this.add(QueryScrollPane, BorderLayout.CENTER);
		
		// construct a search panel
		JPanel SearchPanel = new JPanel();
		JLabel SearchLabel = new JLabel("search");
		SearchPanel.add(SearchLabel);
		SearchPanel.add(SearchText);
		SearchPanel.add(SearchBtn);
		this.SearchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				search();
			}
		});
		this.SearchText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					search();
			}
		});
		this.add(SearchPanel, BorderLayout.SOUTH);
	}
	
	public void resultToPanel_RTree(SecondoObject so, MObjQueryResult mqr) {
		ListExpr numericType, answerList;
		if (mqr.LEResult.isEmpty()) {
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		if (mqr.LEResult.listLength() != 2) {
			Reporter.writeError("laenge nicht 2");
			mqr.addEntry(new String(mqr.LEResult.writeListExprToString()));
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		
		this.addQueryResult(mqr);
		this.QueryCombo.setSelectedItem(mqr);
		DefaultListModel<String> list = (DefaultListModel<String>)(mqr.getModel());
		
		ListExpr attrs = so.toListExpr().second();
		while(!attrs.isEmpty()){
			ListExpr attr = attrs.first();
			attrs = attrs.rest();
			list.addElement(attr.toString());
			list.addElement(" ");
		}
	}	
	public void resultToPanel_MObjs(SecondoObject so, MObjQueryResult mqr) {
		ListExpr numericType, answerList;
		if (mqr.LEResult.isEmpty()) {
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		if (mqr.LEResult.listLength() != 2) {
			Reporter.writeError("laenge nicht 2");
			mqr.addEntry(new String(mqr.LEResult.writeListExprToString()));
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		
		this.addQueryResult(mqr);
		this.QueryCombo.setSelectedItem(mqr);
		DefaultListModel list = (DefaultListModel)(mqr.getModel());
		
		ListExpr first = so.toListExpr().first();
		ListExpr second = so.toListExpr().second();
		
		ListExpr tuples_headers = first.second().second();
		ListExpr tuples = second;
		
		while(!tuples.isEmpty()){
			ListExpr tuple = tuples.first();
			tuples = tuples.rest();
			ListExpr headers = tuples_headers;
			
			while(!headers.isEmpty()){
				ListExpr header = headers.first();
				ListExpr content = tuple.first();
				headers = headers.rest();
				tuple = tuple.rest();
				
				if(header.second().symbolValue().equals("mreal")){
					String str1 = header.first().writeListExprToString().trim()+" : ";
					String str2 = "        [ "+content.first().second().third().writeListExprToString().trim()+" ]";
					list.addElement(str1);
					list.addElement(str2);
					
				}else if(header.second().symbolValue().equals("mstring")
						|| header.second().symbolValue().equals("mint")
						|| header.second().symbolValue().equals("mbool")){
					String str1 = header.first().writeListExprToString().trim()+" : ";
					String str2 = "        [ "+content.first().second().writeListExprToString().trim()+" ]";
					list.addElement(str1);
					list.addElement(str2);
					
				}else if(header.second().symbolValue().equals("mpoint")){
					String str = header.first().writeListExprToString().trim()+" : < mpoint > ";
					list.addElement(str);
					
				}else{
					String str = header.first().writeListExprToString().trim()+" : <"+content.writeListExprToString().trim()+" >";
					list.addElement(str);
				}
			}
			list.addElement("--------------------------------------------------");
			list.addElement(" ");
		}
	}	
	public void resultToPanel_Trajs(SecondoObject so, MObjQueryResult mqr) {
		ListExpr numericType, answerList;
		if (mqr.LEResult.isEmpty()) {
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		if (mqr.LEResult.listLength() != 2) {
			Reporter.writeError("laenge nicht 2");
			mqr.addEntry(new String(mqr.LEResult.writeListExprToString()));
			answerList = ListExpr.twoElemList(ListExpr.intAtom(NOT_ERROR_CODE), ListExpr.theEmptyList());
			return;
		}
		
		this.addQueryResult(mqr);
		QueryCombo.setSelectedItem(mqr);
		DefaultListModel<String> list = (DefaultListModel<String>)(mqr.getModel());
		
		ListExpr tuples = so.toListExpr().second();
		
		if(mqr.getAttrCount()!=2){
			System.err.println("This a error");
			return;
		}else {
			while(!tuples.isEmpty()){
				ListExpr tuple = tuples.first();
				tuples = tuples.rest();
				
				if(tuple.listLength()!=2){
					System.err.println("ERROR");
					return;
				}else{
					String str1 = "Id : "+tuple.first().writeListExprToString();
					list.addElement(str1);
					String str2 = "Start Time : "+tuple.second().first().first().writeListExprToString();
					list.addElement(str2);
					String str3 = "End   Time : "+tuple.second().first().second().writeListExprToString();
					list.addElement(str3);
					String str4 = "Start Location : ( "+tuple.second().second().first().writeListExprToString()
							+"    "+tuple.second().second().second().writeListExprToString()+" )";
					list.addElement(str4);
					String str5 = "End   Location : ( "+tuple.second().second().third().writeListExprToString()
							+"    "+tuple.second().second().fourth().writeListExprToString()+" )";
					list.addElement(str5);
					list.addElement("--------------------------------------------------");
					list.addElement(" ");
				}
			}
		}

	}	
	
	public void addQueryResult(MObjQueryResult mqr) {
		MObjQueryResult q = (MObjQueryResult) QueryCombo.getSelectedItem();
		if (q != null)
			q.clearSelection();
		mqr.setToolTipText(mqr.toString());
		QueryCombo.addItem(mqr);
		QueryCombo.setOpaque(this.isOpaque());
		QueryCombo.setBackground(this.getBackground());
	}
	
	/* set a new ComboBox() */
	public void clearComboBox() {
		remove(QueryCombo);
		QueryCombo = new JComboBox();
		QueryCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MObjQueryResult qr = null;
				qr = (MObjQueryResult) QueryCombo.getSelectedItem();
				if (qr != null) {
					qr.clearSelection();
					QueryScrollPane.setViewportView(qr);
				} else
					QueryScrollPane.setViewportView(dummy);
			}
		});
		add(QueryCombo, BorderLayout.NORTH);
	}

	public static JComboBox getQueryCombo() {
		return QueryCombo;
	}
	
	private void search() {
		System.out.println("search");
	}

}
