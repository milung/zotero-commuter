package sk.mung.sentience.zoteroapi.entities;

public class Creator implements Entity {

    private long id;
	private CreatorType type;
    private Person person = new Person();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

	public CreatorType getType() { return type; }
	public void setType(CreatorType type) { this.type = type; }
	public String getFirstName() { return person.getFirstName(); }
	public void setFirstName(String firstName) { person.setFirstName(firstName); }
	public String getLastName() { return person.getLastName(); }
	public void setLastName(String lastName) { person.setLastName(lastName); }
	public String getShortName() { return  person.getShortName(); }
	public void setShortName(String shortName) { person.setShortName(shortName); }

    public Person getPerson() { return person;}
    public boolean isValid()
    {
        return person.isValid();
    }
}
