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

import scala.concurrent.{Future, Promise, ExecutionContext}
import java.util.concurrent.ConcurrentLinkedQueue

sealed class Bottleneck[A, B](size: Int)(f: A => Future[B])(
    implicit ec: ExecutionContext) {

  private case class Elem[A, B](a: A, p: Promise[B])

  private val queue = new ConcurrentLinkedQueue[Elem[A, B]]()
  private var slots =
    scala.collection.mutable.ArrayBuffer.fill[Option[Future[B]]](size)(None)
  private def send(x: Elem[A, B]): Unit = {
    val idx = slots.indexWhere(_.isEmpty)
    if (idx >= 0) {
      val fut = try { f(x.a) } catch {
        case e: Exception => Future.failed(e)
      }
      slots(idx) = Some(fut)

      fut.onComplete {
        case y =>
          x.p.complete(y)
          done(idx)
          take
      }

    } else {
      queue.add(x)
    }
  }
  private def done(idx: Int) = {
    slots(idx) = None
  }
  private def take = {
    val h = queue.poll
    if (h != null) {
      send(h)
    }
  }

  def apply(h: A) = {
    val p = Promise[B]()
    send(Elem(h, p))
    p.future
  }

}
