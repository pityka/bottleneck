/*
 * The MIT License
 *
 * Copyright (c) 2016 Istvan Bartha
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package bottleneck

import org.scalatest._

import akka.actor._
import org.scalatest.FunSpec
import org.scalatest.Matchers
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

class TestSuite extends FunSuite with Matchers {

  def await[T](f: Future[T]) = Await.result(f, atMost = Duration.Inf)

  test("1") {
    implicit val as =
      ActorSystem("test1", ConfigFactory.parseString("akka.loglevel=DEBUG"))
    val fun = (a: Int) =>
      akka.pattern.after(1 seconds, as.scheduler)(Future(a.toString + "boo"))

    val queue = new Bottleneck(1)(fun)
    val t1 = System.nanoTime / 1E9
    await(queue(1)) should equal("1boo")
    await(Future.sequence((1 to 10).toList map (i => queue(i)))) should equal(
      (1 to 10).toList.map(_ + "boo"))
    val t2 = System.nanoTime / 1E9
    (t2 - t1) < 12 should equal(true)
    (t2 - t1) > 10 should equal(true)
    as.shutdown
  }

  test("5") {
    implicit val as =
      ActorSystem("test1", ConfigFactory.parseString("akka.loglevel=DEBUG"))
    val fun = (a: Int) =>
      akka.pattern.after(1 seconds, as.scheduler)(Future(a.toString + "boo"))

    val queue = new Bottleneck(5)(fun)
    val t1 = System.nanoTime / 1E9
    await(Future.sequence((1 to 10).toList map (i => queue(i)))) should equal(
      (1 to 10).toList.map(_ + "boo"))
    val t2 = System.nanoTime / 1E9
    (t2 - t1) < 3 should equal(true)
    (t2 - t1) > 2 should equal(true)
    as.shutdown
  }

  test("3") {
    implicit val as =
      ActorSystem("test1", ConfigFactory.parseString("akka.loglevel=DEBUG"))
    val fun = (a: Int) =>
      akka.pattern.after(1 seconds, as.scheduler)(Future(a.toString + "boo"))

    val queue = new Bottleneck(3)(fun)
    val t1 = System.nanoTime / 1E9
    await(Future.sequence((1 to 9).toList map (i => queue(i)))) should equal(
      (1 to 9).toList.map(_ + "boo"))
    val t2 = System.nanoTime / 1E9
    (t2 - t1) < 4 should equal(true)
    (t2 - t1) > 3 should equal(true)
    as.shutdown
  }

  test("100000") {
    implicit val as =
      ActorSystem("test1", ConfigFactory.parseString("akka.loglevel=DEBUG"))
    val fun = (a: Int) =>
      akka.pattern.after(10 milliseconds, as.scheduler)(
        Future(a.toString + "boo"))

    val queue = new Bottleneck(100)(fun)
    await(Future.sequence((1 to 100000).toList map (i => queue(i)))) should equal(
      (1 to 100000).toList.map(_ + "boo"))

    as.shutdown
  }

}
