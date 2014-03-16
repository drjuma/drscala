package drscala
package rules

import PhaseId._

/**
 Example AST exploration:

scala> import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe._

scala> val exp = reify { def f(x: Int, y: Int) = x != y }
scala> show(exp.tree)
scala> showRaw(exp.tree)

**/

object TimeOutRules extends RuleSet("to") with RuleSet.DSL {
  override def rules = {
    Phase(Parser) { phase => import phase._
      Seq(
        CheckSementic("UnsafeEq")(ctx => new TreeCheck(ctx) { import universe._
          def apply = _.collect { 
            case tree@Select(_, name) if name.toString == "$eq$eq" => 
              tree.signal("Why not using `===` instead of `==`?")
          }
        }),
        CheckSementic("UnsafeDiff")(ctx => new TreeCheck(ctx) { import universe._
          def apply = _.collect { 
            case tree@Select(_, name) if name.toString == "$bang$eq" => 
              tree.signal("Why not using `=!=` instead of `!=`?")
          }
        })
      )
    } ++
    Phase(Typer) { phase => import phase._
      Seq(
        CheckSementic("StringSplit")(ctx => new TreeCheck(ctx) { import universe._
          def apply = _.collect {
            case tree@Select(value, name) if name.toString == "split" && value.tpe <:< typeOf[String] => 
              tree.signal(
                "You should prefer `splitBy`, which don't use a regex and have better performance.\n" +
                "If you want to use a regex it's always better to precompile and cache it."
              )
          }
        })
      )
    }
  }
}
