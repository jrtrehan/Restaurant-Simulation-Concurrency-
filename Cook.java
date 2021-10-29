package cmsc433;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Customer and process them.
 */
public class Cook implements Runnable {
	private final String name;
	//public static Queue<Order> currentOrder; 
	public static CountDownLatch secondLatch = new CountDownLatch(1);
	public static HashMap<Integer, List<Food>> completedItems = new HashMap<Integer, List<Food>>(); 
	/**
	 * You can feel free modify this constructor. It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful.
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
	

	/**
	 * This method executes as follows. The cook tries to retrieve
	 * orders placed by Customers. For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine type, by calling makeFood(). Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified. The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while (true) {
				// YOUR CODE GOES HERE..
				Order temp;
				synchronized(Simulation.currentOrder) {
					while(Simulation.currentOrder.isEmpty()) {
						Simulation.currentOrder.wait();
					}
					temp = Simulation.currentOrder.remove(0);
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, temp.order, temp.orderNum)); 	
				}
				
				//GO THROUGH ORDER 
				List<Thread> items = new ArrayList<Thread>();
				for(int i = 0; i < temp.order.size(); i++) {
					//REQUEST MACHINE TO PROCESS ITEMS IN ORDER 
					if(temp.order.get(i).equals(FoodType.fries)) {
						items.add(Simulation.fryers.makeFood(temp.orderNum));
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.fries, temp.orderNum));
					} else if(temp.order.get(i).equals(FoodType.pizza)) {
						items.add(Simulation.ovens.makeFood(temp.orderNum));
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.pizza, temp.orderNum));
					} else if(temp.order.get(i).equals(FoodType.subs)) {
						items.add(Simulation.grillPresses.makeFood(temp.orderNum));
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.subs, temp.orderNum));
					} else {
						items.add(Simulation.sodaMachines.makeFood(temp.orderNum));
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.soda, temp.orderNum));
					}
					
				}
				
				//another for loop to go through threads list 
				//call join on all the threads
				for(int i = 0; i < items.size(); i++) {
					items.get(i).join();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(this, temp.order.get(i), temp.orderNum));
				}
				Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, temp.orderNum));
				items.clear();
				//WAIT FOR ORDER TO FINISH
				synchronized(Simulation.completedOrders) {
					Simulation.completedOrders.add(temp.orderNum);
					Simulation.completedOrders.notifyAll();
				}
				//SEND COMPLETE ORDER TO CUSTOMER
				
				//CountDown after order is done
				//Simulation.logEvent(SimulationEvent.cookFinishedFood(this, temp.order, temp.orderNum));
				//Customer.latch.countDown();

				//throw new InterruptedException(); // REMOVE THIS
			}
		} catch (InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}
