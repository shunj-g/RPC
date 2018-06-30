package cn.group.sample.client;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018Äê6ÔÂ30ÈÕ
 */
public class Person {

	private String firstName;
	private String lastName;
	
	public Person(){
		
	}
	public Person(String firstName,String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
