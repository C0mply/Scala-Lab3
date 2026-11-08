import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object Lab3 {
  // Task 1: preserve the assignment's Seq[Int] signature and 1,2,3,... order.
  // An overflowing Int is rejected instead of silently wrapping around.
  def fibs(count: Int): Seq[Int] = {
    require(count >= 0, "The number of terms must not be negative")
    @tailrec
    def loop(left: Int, previous: Int, current: Int,
             reversed: List[Int]): List[Int] = {
      if (left == 0) reversed.reverse
      else {
        val next = Math.addExact(previous, current)
        loop(left - 1, current, next, next :: reversed)
      }
    }
    loop(count, 0, 1, Nil)
  }

  case class Address(street: String, city: String, state: String, zipCode: String)
  case class Person(first: String, last: String, address: Option[Address])

  // Task 2: explicit map/flatMap equivalent, with no for comprehension.
  def zipCodes(people: Option[Seq[Option[Person]]]): Seq[String] =
    people.toSeq.flatMap { entries =>
      entries.flatMap { personOption =>
        personOption.toSeq.flatMap { person =>
          person.address.toSeq.map { address => address.zipCode }
        }
      }
    }

  def zipCodesFor(people: Option[Seq[Option[Person]]]): Seq[String] =
    for {
      entries <- people.toSeq
      personOption <- entries
      person <- personOption.toSeq
      address <- person.address.toSeq
    } yield address.zipCode

  // Task 3: intentionally leave the return type inferred, exactly as assigned.
  def safeDivOriginal(n: Int, d: Int) = if (d == 0) None else n / d

  // Task 4: both branches now have the common, useful type Option[Int].
  // Division truncates toward zero, following Scala Int division.
  def safeDiv(n: Int, d: Int): Option[Int] =
    if (d == 0) None else Some(n / d)

  // Task 5: match with a guard; no negation or abs that could overflow.
  def describeSign(n: Int): String = n match {
    case 0 => "Zero"
    case value if value > 0 => "Positive"
    case _ => "Negative"
  }

  // Task 6: no Await, blocking, or sleep inside these asynchronous functions.
  def answerAsync(a: Future[Int], b: Future[Int], c: Future[Int],
                  text: Future[String])(implicit ec: ExecutionContext): Future[String] =
    for {
      av <- a
      bv <- b
      cv <- c
      prefix <- text
      sum <- Future(av + bv + cv)
    } yield s"$prefix $sum"

  def answerWithFlatMap(a: Future[Int], b: Future[Int], c: Future[Int],
                        text: Future[String])
                       (implicit ec: ExecutionContext): Future[String] =
    a.flatMap(av => b.flatMap(bv => c.flatMap(cv =>
      text.flatMap(prefix => Future(av + bv + cv).map(sum => s"$prefix $sum")))))

  def check(label: String)(condition: => Boolean): Unit = {
    assert(condition, label)
    println(s"PASS: $label")
  }

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    println("SCALA LABORATORY WORK 3 - BEST PRACTICES AND IDIOMS")
    println("Author: Andrew Galashin")
    println(s"Scala ${scala.util.Properties.versionNumberString}; Java ${System.getProperty("java.version")}")
    println("TASK 1 - Immutable tail-recursive Fibonacci")
    println(s"fibs(10) = ${fibs(10)}")
    check("supplied ten-term sequence") {
      fibs(10) == List(1, 2, 3, 5, 8, 13, 21, 34, 55, 89)
    }
    check("zero, one and two terms") {
      fibs(0).isEmpty && fibs(1) == List(1) && fibs(2) == List(1, 2)
    }
    check("negative term count rejected") {
      Try(fibs(-1)).failed.toOption.exists(_.isInstanceOf[IllegalArgumentException])
    }
    check("Int boundary and overflow detection") {
      fibs(45).last == 1836311903 &&
        Try(fibs(46)).failed.toOption.exists(_.isInstanceOf[ArithmeticException])
    }
    check("length and recurrence for all supported counts 0 to 45") {
      (0 to 45).forall { n =>
        val xs = fibs(n)
        xs.length == n && xs.sliding(3).filter(_.length == 3).forall(t => t(2) == t(0) + t(1))
      }
    }

    println("TASK 2 - Options and collections using flatMap and map")
    val p1 = Some(Person("Fred", "Bloggs", None))
    val p2 = Some(Person("Simon", "Jones",
      Some(Address("123 Main", "Fakesville", "AZ", "12345"))))
    val p3: Option[Person] = None
    val people: Option[Seq[Option[Person]]] = Some(Seq(p1, p2, p3))
    println(s"zipCodes = ${zipCodes(people)}")
    check("explicit expression equals for comprehension") {
      zipCodes(people) == Seq("12345") && zipCodes(people) == zipCodesFor(people)
    }
    check("absent outer option, empty sequence and missing values") {
      zipCodes(None).isEmpty && zipCodes(Some(Seq.empty)).isEmpty &&
        zipCodes(Some(Seq(p1, p3))).isEmpty
    }
    check("multiple addresses preserve order and duplicates") {
      zipCodes(Some(Seq(p2, p2))) == Seq("12345", "12345")
    }

    println("TASK 3 - Inferred return type of the original safeDiv")
    println("Scala 2.13 compiler inference: Any (see evidence/Type-inference.txt)")
    println(s"safeDivOriginal(5, 0) = ${safeDivOriginal(5, 0)}")
    println(s"safeDivOriginal(5, 2) = ${safeDivOriginal(5, 2)}")
    check("original returns None or a bare Int, not Option[Int]") {
      safeDivOriginal(5, 0) == None && safeDivOriginal(5, 2) == 2
    }

    println("TASK 4 - Correct Option[Int] return type and flatten")
    val mapped = List(0, 1, 2).map(safeDiv(5, _))
    println(s"map = $mapped")
    println(s"flatten = ${mapped.flatten}")
    check("safe division and flatMap") {
      mapped == List(None, Some(5), Some(2)) && mapped.flatten == List(5, 2) &&
        List(0, 1, 2).flatMap(safeDiv(5, _)) == List(5, 2)
    }
    check("negative operands and zero numerator") {
      safeDiv(-5, 2).contains(-2) && safeDiv(5, -2).contains(-2) && safeDiv(0, 7).contains(0)
    }

    println("TASK 5 - Pattern matching with a guard")
    List(-5, 0, 7).foreach(n => println(s"describeSign($n) = ${describeSign(n)}"))
    check("negative, zero, positive and Int extremes") {
      describeSign(-5) == "Negative" && describeSign(0) == "Zero" &&
        describeSign(7) == "Positive" && describeSign(Int.MinValue) == "Negative" &&
        describeSign(Int.MaxValue) == "Positive"
    }

    println("TASK 6 - Fully asynchronous composition")
    // Independent futures are created before composition, so they can run concurrently.
    val a = Future(1)
    val b = Future(2)
    val c = Future(3)
    val text = Future("The answer is")
    val result = answerAsync(a, b, c, text)
    // Only the terminal/test boundary waits, keeping the JVM alive for results.
    val value = Await.result(result, 5.seconds)
    println(s"Future result = $value")
    check("for and flatMap versions produce The answer is 6") {
      value == "The answer is 6" &&
        Await.result(answerWithFlatMap(a, b, c, text), 5.seconds) == value
    }
    val pending = Promise[Int]()
    val deferred = answerAsync(pending.future, b, c, text)
    check("composition returns before an unresolved input is completed") {
      !deferred.isCompleted
    }
    pending.success(1)
    check("pending input eventually yields the correct result") {
      Await.result(deferred, 5.seconds) == "The answer is 6"
    }
    val failure = new IllegalStateException("input failed")
    val failed = answerAsync(Future.failed(failure), b, c, text)
    check("a failed input is propagated as the same exception") {
      Await.result(failed.failed, 5.seconds) eq failure
    }
    println("ALL CHECKS PASSED")
  }
}
