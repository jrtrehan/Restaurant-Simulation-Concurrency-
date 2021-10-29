package cmsc433;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order. When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	public static int tableCount;
	private static int runningCounter = 0;
	public static Semaphore sem;
	public static CountDownLatch latch = new CountDownLatch(1);

	/**
	 * You can feel free modify this constructor. It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int tableCount) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
		Customer.tableCount = tableCount;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 */
	public void run() {
		
		// YOUR CODE GOES HERE...
		//before entering Ratsie's
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		//check if table is available 
		try {
			
			//wait until table is available 
			sem.acquire();
			//entered Ratsies
			Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this)); 
			
			synchronized(Simulation.currentOrder) {
				//place order 
				Order temp = new Order(this.order, this.orderNum); 
				Simulation.currentOrder.add(temp);
				Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, this.order, this.orderNum));
				Simulation.currentOrder.notifyAll();
			}
			//wait for order
			synchronized(Simulation.completedOrders) {
				while(!Simulation.completedOrders.contains(this.orderNum)) {
					Simulation.completedOrders.wait();
				}
			}
			
			//Receive order and eat food 
			Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, this.order, this.orderNum));
			//leave restaurant 
			sem.release();
			Simulation.logEvent(SimulationEvent.customerLeavingRatsies(this));
		} catch (InterruptedException e) {
			System.out.println("Entered Catch");
		}
	}
}
