package example.frontend

import utest.TestSuite
import utest.Tests
import utest.test

object ClientSpec extends TestSuite  {

  override def tests: Tests = Tests{
      test("nothing"){
          1 == 1
      }
  }

}
