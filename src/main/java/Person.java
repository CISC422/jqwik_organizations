/* CISC/CMPE 422/835
 * Implementation of simple class for persons
 */

import java.time.LocalDate;

public class Person implements Comparable<Person> {

    public final String firstName;
    public final String lastName;
    public int age;
    public LocalDate birthday;
    public Email emailAddress;
    public Person parent = null;

    public Person(String fName, String lName, LocalDate dob, Email email) {
        if (fName == null || fName.trim().isEmpty())
            throw new IllegalArgumentException();
        if (lName == null || lName.trim().isEmpty())
            throw new IllegalArgumentException();
        this.firstName = fName;
        this.lastName = lName;
        this.birthday = dob;
        this.age = LocalDate.now().getYear() - dob.getYear();
        this.emailAddress = email;
    }

    public void updateParent(Person parent) {
        this.parent = parent;
    }

    public int compareTo(Person p) {
        return this.lastName.compareTo(p.lastName);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%d, %s) <%s> [Parent: %s]", firstName, lastName, age, birthday, emailAddress, parent);
    }
}