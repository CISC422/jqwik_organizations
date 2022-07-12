/* CISC/CMPE 422/835
 * Implementation of simple class for organizations
 */
import java.util.Collections;
import java.util.List;

public class Organization {

    public final String name;
    public List<Person> members;
    public int numMembers;

    public Organization(String name, List<Person> members) {
        this.name = name;
        this.members = members;
        this.numMembers = this.members.size();
    }

    public void sort() {
        Collections.sort(this.members);
    }

    public void addMember (Person p) {
        this.members.add(p);
        this.numMembers++;
    }

    @Override
    public String toString() {
        return "Organization {" +
                "name='" + name + "'" +
                ", members=\n" + members +
                '}';
    }
}