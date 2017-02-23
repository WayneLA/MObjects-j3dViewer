package viewer.mobject;

import java.util.Vector;

import gui.idmanager.ID;
import sj.lang.ListExpr;
import viewer.hoese.Interval;
import viewer.hoese.LEUtils;

/** 
 * @ClassName: DynamicAttribute 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author wangweiwei_LA 
 * @date 2016年11月2日 上午11:23:04
 *  
*/
public class DynamicAttribute {
	
	private ID id;
	private Vector<Interval> TimeBounds = new Vector<Interval>(0);
	private Vector attributes = new Vector(0);
	
	public int cti;
	
	public DynamicAttribute(ID Id, ListExpr LE){
		super();
		this.id = Id;
		this.cti = -1;
		
		while(!LE.isEmpty()){
			ListExpr attrtuple = LE.first();
			LE = LE.rest();

			ListExpr timebounds = attrtuple.first();
			ListExpr attr = attrtuple.second();
			
			double st, et;
			boolean lt, rt;
			lt = timebounds.third().boolValue();
			rt = timebounds.fourth().boolValue();
			st = LEUtils.readInstant(timebounds.first());
			et = LEUtils.readInstant(timebounds.second());
			
			this.TimeBounds.add(new Interval(st, et, lt, rt));
			
			if(attr.listLength() != 4){
				this.attributes.add(attr.writeListExprToString().trim());
			}else{
				this.attributes.add(attr.third().writeListExprToString().trim());
			}
			
		}
	}
	
	public int updateDattr(double ct){
		int index = -1;
		if(ct<((Interval) TimeBounds.get(0)).getStart()){
			return index;
		}
		if (ct > ((Interval) TimeBounds.get(TimeBounds.size() - 1)).getEnd()){
			return index;
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
				if(cur == start){
					return -3;
				}
				long st = (new Double(t.getStart() * 86400000)).longValue();
				long et = (new Double(t.getEnd() * 86400000)).longValue();
				if (l_ct <= st){
					end = cur;
				}
				else if (l_ct >= et)
				{
					start = cur;
				}
			}
		}
		if (index < 0) {
			return -2;
		}
		return index;
	}

	public ID getId() {
		return id;
	}
	public void setId(ID id) {
		this.id = id;
	}
	public Vector<Interval> getTimeBounds() {
		return TimeBounds;
	}
	public void setTimeBounds(Vector<Interval> timeBounds) {
		TimeBounds = timeBounds;
	}
	public Vector getAttributes() {
		return attributes;
	}
	public void setAttributes(Vector attributes) {
		this.attributes = attributes;
	}
	public int getCti() {
		return cti;
	}
	public void setCti(int cti) {
		this.cti = cti;
	}
	
	@Override
	public String toString() {
		return "DynamicAttribute [id=" + id + ", TimeBounds=" + TimeBounds + ", attrs=" + attributes + ", cti=" + cti + "]";
	}
	
}
