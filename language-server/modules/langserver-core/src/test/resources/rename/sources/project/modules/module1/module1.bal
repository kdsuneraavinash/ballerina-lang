import project.module2;

# School
public class School {
    private string name;
    private module2:Person[] students = [];

    public function getStudents() returns module2:Person[] {
        return self.students;
    }

    public function addStudent(module2:Person p) {
        self.students.push(p);
    }
}
