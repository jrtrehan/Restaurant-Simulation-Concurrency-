package cmsc433;

import java.util.List;

public class Order {

	public List<Food> order; 
	public int orderNum; 
	
	public Order(List<Food> order, int orderNum) {
		this.order = order; 
		this.orderNum = orderNum; 
	}
	
	public List<Food> getOrder(){
		return this.order;
	}
	
}
