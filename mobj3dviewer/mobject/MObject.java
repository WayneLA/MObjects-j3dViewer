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

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import java.util.Vector;
import viewer.hoese.*;

public class MObject {
	private int id;
	private Vector<Interval> TimeBounds;
	private Vector3d[] locations_start, locations_end;
	
	private PointArray point_geometry = new PointArray(1, PointArray.COORDINATES|PointArray.COLOR_3);
	private PointAttributes point_attribute = new PointAttributes();
	private Appearance point_appearence = new Appearance();
	private float point_size = 8.0f;
	private Color3f point_color = COLOR_WRITE;
	
	public Shape3D point_shape3D = new Shape3D();
	
	//Color
	public final static Color3f COLOR_RED = new Color3f(1.0f, 0.0f, 0.0f);
	public final static Color3f COLOR_GREEN = new Color3f(0.0f, 1.0f, 0.0f);
	public final static Color3f COLOR_BLUE = new Color3f(0.0f, 0.0f, 1.0f);
	public final static Color3f COLOR_BLACK = new Color3f(0.0f, 0.0f, 0.0f);
	public final static Color3f COLOR_WRITE = new Color3f(1.0f, 1.0f, 1.0f);
	public final static Color3f COLOR_YELLOW = new Color3f(1.0f, 1.0f, 0.0f);
	public final static Color3f COLOR_CYAN = new Color3f(0.0f, 1.0f, 1.0f);
	public final static Color3f COLOR_MAGENTA = new Color3f(1.0f, 0.0f, 1.0f);
	
	public MObject(int id, Vector3d[] loc1, Vector3d[] loc2, Vector<Interval> times) {
		this.id = id;
		this.locations_start = new Vector3d[loc1.length];
		this.locations_end = new Vector3d[loc2.length];
		assert (loc1.length == loc2.length);
		TimeBounds = new Vector<Interval>(0);
		for (int i=0; i<loc1.length; i++) {
			this.locations_start[i] = loc1[i];
			this.locations_end[i] = loc2[i];
			this.TimeBounds.add((Interval) times.get(i));
		}
		
		point_geometry.setCapability(PointArray.ALLOW_COLOR_READ);
		point_geometry.setCapability(PointArray.ALLOW_COLOR_WRITE);
		point_geometry.setCapability(Geometry.ALLOW_INTERSECT);
		
		point_attribute.setPointAntialiasingEnable(true);//反走样
		
		point_shape3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		point_shape3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		point_shape3D.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		point_shape3D.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		
		point_shape3D.setName(String.valueOf(id));
	}
	
	public Shape3D CreateObj() {
		point_geometry.setColor(0, point_color);//color
		point_attribute.setPointSize(point_size);//size
		point_appearence.setPointAttributes(point_attribute);

		point_shape3D.setGeometry(point_geometry);
		point_shape3D.setAppearance(point_appearence);
		
		return point_shape3D;
	}
	
	public Transform3D UpdateLoc(double ct) {
		int index = -1;
		if (ct > ((Interval) TimeBounds.get(TimeBounds.size() - 1)).getEnd()){
			return null;
		}
		long l_ct = (new Double(ct * 86400000)).longValue();
		int start = 0;
		int end = TimeBounds.size();
		
		while (start <= end) {
			int cur = (start + end) / 2;
			Interval t = (Interval) TimeBounds.get(cur);
			if (t.isDefinedAt(ct)) {
				index = cur;
				break;
			} else {
				long st = (new Double(t.getStart() * 86400000)).longValue();
				long et = (new Double(t.getEnd() * 86400000)).longValue();
				if (l_ct < st || l_ct == st){
					end = cur;
				}
				else if (l_ct > et || l_ct == et)
				{
					start = cur;
				}
				else{
					assert (false);
				}
			}
		}
		if (index < 0) {
			return null;
		}

		double t1 = ((Interval) TimeBounds.get(index)).getStart();
		double t2 = ((Interval) TimeBounds.get(index)).getEnd();
		double Delta = (ct - t1) / (t2 - t1);

		double delta_x = locations_end[index].x - locations_start[index].x;
		double delta_y = locations_end[index].y - locations_start[index].y;
		double delta_z = locations_end[index].z - locations_start[index].z;
		
		double tempx = locations_start[index].x + delta_x * Delta;
		double tempy = locations_start[index].y + delta_y * Delta;
		double tempz = locations_start[index].z + delta_z * Delta;
		
		Vector3d v3d = new Vector3d(tempx,tempy,tempz);
		Transform3D trans3d = new Transform3D();
		trans3d.set(v3d);
		
		return trans3d;
	}
	
	public Transform3D getStartTrans3D(){
		Vector3d v3d = locations_start[0];
		Transform3D trans3d = new Transform3D();
		trans3d.set(v3d);
		return trans3d;
	}
	
	public double getStart() {
		return ((Interval) TimeBounds.get(0)).getStart();
	}
	public double getEnd() {
		return ((Interval) TimeBounds.get(TimeBounds.size() - 1)).getEnd();
	}

}
