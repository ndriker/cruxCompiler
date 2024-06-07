package crux;

final class Authors {
  // TODO: Add author information.
  static final Author[] all = {new Author("Noah Driker", "94574697", "ndriker"),
                               new Author("Jason Tran", "53507694", "jasont12")};
}


final class Author {
  final String name;
  final String studentId;
  final String uciNetId;

  Author(String name, String studentId, String uciNetId) {
    this.name = name;
    this.studentId = studentId;
    this.uciNetId = uciNetId;
  }
}
