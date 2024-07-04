package info.kgeorgiy.ja.shinkareva.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private final static Comparator<Student> STUDENT_NAME_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).thenComparing(Student::getId, Comparator.reverseOrder());

    private final static Comparator<Group> GROUP_NAME_COMPARATOR = Comparator.comparing(Group::getName);

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroups(students, st -> st.stream().sorted(STUDENT_NAME_COMPARATOR).collect(Collectors.toList()));
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroups(students, st -> st.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList())); // NOTE: natural order FIXED
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupName(students, List::size, true);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupName(students, st -> getDistinctFirstNames(st).size(), false);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentsList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentsList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getStudentsList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentsList(students, student -> String.format("%s %s", student.getFirstName(), student.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getMapedStream(students, Student::getFirstName).sorted(String::compareTo).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.comparingInt(Student::getId)).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, STUDENT_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private <T> Stream<T> getMapedStream(Collection<Student> students, Function<Student, T> function) {
        return students.stream().map(function);
    }

    private <T> List<T> getStudentsList(Collection<Student> students, Function<Student, T> function) {
        return getMapedStream(students, function).collect(Collectors.toList());
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> cmp) {
        return students.stream().sorted(cmp).collect(Collectors.toList());
    }

    private List<Student> findStudentsBy(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate).sorted(STUDENT_NAME_COMPARATOR).collect(Collectors.toList());
    }

    private Stream<Map.Entry<GroupName, List<Student>>> getGroupedStudents(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup)) // Map<GroupName, List<Student>>
                .entrySet() // Set<Map.Entry<GroupName, List<Student>>>
                .stream();
    }

    private List<Group> getSortedGroups(Collection<Student> students, Function<List<Student>, List<Student>> function) {
        return getGroupedStudents(students)
                .map(entry -> new Group(entry.getKey(), function.apply(entry.getValue()))) // sorted students in every group
                .sorted(GROUP_NAME_COMPARATOR) // sorted groups // NOTE: create compare for every method call FIXED
                .collect(Collectors.toList());
    }

    private GroupName getLargestGroupName(Collection<Student> students, Function<List<Student>, Integer> function, boolean largestIfEqual) {
        return getGroupedStudents(students)
                .map(entry -> Map.entry(entry.getKey(), function.apply(entry.getValue())))
                .sorted(Map.Entry.comparingByKey(largestIfEqual ? Collections.reverseOrder() : Comparator.naturalOrder()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
