/* CISC/CMPE 422/835
 * Property-based testing of parts of simple implementation of organization application
 */

import net.jqwik.api.*;
import net.jqwik.time.api.Dates;
import net.jqwik.web.api.Web;
import org.assertj.core.api.Assertions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrganizationTestProperties {

    public boolean checkSorted(List<Person> persons) {
        int len = persons.size();
        for (int i = 0; i < len - 1; i++)
            if (persons.get(i).compareTo(persons.get(i + 1)) > 0)
                return false;
        return true;
    }

    public boolean checkAncestryIsAcyclic(Person p) {
        return checkAncestryIsAcyclic(p, new ArrayList<>());
    }

    public boolean checkAncestryIsAcyclic(Person p, List<Person> visited) {
        if (visited.contains(p))
            return false;
        else
            if (p.parent != null) {
                visited.add(p);
                return checkAncestryIsAcyclic(p.parent, visited);
            }
            else
                return true;
    }

    // PROPERTIES ==========================================================

    // PROPERTIES: for manual inspection of generated values ---------------
    @Property
    @Report(Reporting.GENERATED)
//    void propertyCheckPersonNoParentGenerator1(@ForAll("personsNoParent") Person aPerson) {
    void propertyCheckPersonNoParentGenerator1(@ForAll("personsWithParent") Person aPerson) {
        System.out.println(aPerson);
    }

    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckOrganizationGenerator1(@ForAll("organizations") Organization aOrganization) {
        System.out.println(aOrganization);
    }

    // PROPERTIES: for checking validity of generated values -------------------

    // generated dates are valid birthdays
    @Property
    void propertyDateTimesAreValid (@ForAll("dates") LocalDate localDate) {
        System.out.println(localDate);
        Assertions.assertThat(localDate).isAfter(LocalDate.of(1899, 5, 25));
    }

    // generated persons have a first name and an age
    @Property
    void propertyPersonsIsAlwaysValid(@ForAll("personsWithParent") Person aPerson) {
        System.out.println(aPerson);
        Assertions.assertThat(aPerson.firstName).isNotBlank();
        Assertions.assertThat(aPerson.age).isBetween(0, 130);
    }

    // generated persons have valid name
    @Property
    @Report(Reporting.GENERATED)
    void propertyPersonsHaveShortNameWithSpace(@ForAll("personsWithParent") Person aPerson) {
        System.out.println(aPerson);
        // Assertions.assertThat(aPerson.lastName).contains(" ");  // fails
        Assertions.assertThat(aPerson.lastName.length()).isBetween(2, 21);
    }

    // generated persons with parents have an age less than that of their parent
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckPersonWithParentGenerator1(@ForAll("personsWithParent") Person aPerson) {
        if (aPerson.parent != null)
            Assume.that(aPerson.parent.age > aPerson.age);
        System.out.println(aPerson);
    }

    // generated persons don't have themselves as parent
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckPersonGenerator3(@ForAll("personsWithParent") Person aPerson) {
        System.out.println(aPerson);
        Assertions.assertThat(checkAncestryIsAcyclic(aPerson)).isTrue();
    }

    // PROPERTIES: for checking operations -------------------

    // updating parents will not create persons that have themselves as parent (fails)
    @Property
    @Report(Reporting.GENERATED)
    void propertyAddParent1(@ForAll("personsWithParent") Person p1,
                            @ForAll("personsWithParent") Person p2) {
        System.out.println(p1);
        p1.updateParent(p2);
        p2.updateParent(p1);
        Assertions.assertThat(checkAncestryIsAcyclic(p1)).isTrue();
    }

    // adding a member increases membership count by 1
    @Property
    @Report(Reporting.GENERATED)
    public void propertyAddMember1(@ForAll("organizations") Organization aOrganization,
                                   @ForAll("personsWithParent") Person p) {
        System.out.println(aOrganization);
        int num0 = aOrganization.numMembers;
        aOrganization.addMember(p);
        System.out.println(aOrganization);
        Assertions.assertThat(aOrganization.numMembers).isEqualTo(num0 + 1);
    }

    // adding a member results in a membership list that contains the added member
    @Property
    @Report(Reporting.GENERATED)
    public void propertyAddMember2(@ForAll("organizations") Organization aOrganization,
                                   @ForAll("personsWithParent") Person p) {
        System.out.println(aOrganization);
        aOrganization.addMember(p);
        System.out.println(aOrganization);
        Assertions.assertThat(aOrganization.members).contains(p);
    }

    // method 'sort' really does sort
    @Property
    @Report(Reporting.GENERATED)
    void propertySort1(@ForAll("organizations") Organization aOrganization) {
        System.out.println(aOrganization);
        aOrganization.sort();
        System.out.println(aOrganization);
        Assertions.assertThat(checkSorted(aOrganization.members)).isTrue();
    }

    // adding a member does not necessarily preserve sortedness (fails)
    @Property
      @Report(Reporting.GENERATED)
    public void propertyAddAndSort1(@ForAll("organizations") Organization aOrganization,
                                    @ForAll("personsWithParent") Person p) {
        System.out.println(aOrganization);
        aOrganization.sort();
        aOrganization.addMember(p);
        System.out.println(aOrganization);
        Assertions.assertThat(checkSorted(aOrganization.members)).isTrue();
    }


    // GENERATORS ==============================================================

    @Provide
    Arbitrary<Organization> organizations() {
        Arbitrary<List<Person>> personList = personsWithParent().list();
        return Combinators.combine(
                strings(), personList)
                .as((pName, persL) -> new Organization(pName, persL));
    }

    // GENERATORS: Persons
    @Provide
    Arbitrary<Person> personsNoParent() {
        return Combinators.combine(strings(), strings(), dates(), emails())
                .as((firstN,lastN,dob,email) -> new Person(firstN,lastN,dob,email));
    }

    @Provide
    Arbitrary<Person> personsWithParent() {
        Arbitrary<Integer> count = Arbitraries.integers().between(0, 3);
        return count.flatMap(c -> personsWithParent(c, personsNoParent()));
    }

    @Provide
    Arbitrary<Person> personsWithParent(int count, Arbitrary<Person> personsWithParent) {
        if (count == 0)
            return personsWithParent;
        else {
            Arbitrary<Person> nextLevel =
                    Combinators.combine(personsNoParent(), personsWithParent)
                            .as((pNoP, pWP) -> {
                                pNoP.parent = pWP;
                                return pNoP;
                            });
            return personsWithParent(count-1, nextLevel);
        }
    }

    // GENERATORS: strings, dates, and emails
    @Provide
    Arbitrary<String> strings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(10)
                .map(s -> s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
    }

    @Provide
    Arbitrary<LocalDate> dates() {
        return Dates
                .dates()
                .atTheEarliest(LocalDate.of(1900, 1, 1))
                .atTheLatest(LocalDate.of(2021, 12, 31));
    }
    @Provide
    Arbitrary<Email> emails() {
        return Web.emails().map(e -> new Email(e));
    }


}
