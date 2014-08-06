package worksheets
import provingground.AgdaExpressions._
import scala.util.parsing.combinator._

object AgdaParseTest {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  val parser = new AgdaParse                      //> parser  : provingground.AgdaExpressions.AgdaParse = provingground.AgdaExpres
                                                  //| sions$AgdaParse@2bb8ee96

	import parser._
	
	parseAll(expr, "x")                       //> res0: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.2] parsed: Token(x)
  parseAll(expr, "::")                            //> res1: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.3] parsed: Token(::)
  
  parseAll(To, "->")                              //> res2: worksheets.AgdaParseTest.parser.ParseResult[String] = [1.3] parsed: ->
                                                  //| 
  parseAll(expr, "x -> y")                        //> res3: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.7] parsed: Arrow(Token(x),Token(y))
  parseAll(term, "x")                             //> res4: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.2] parsed: Token(x)
  arrow()                                         //> res5: worksheets.AgdaParseTest.parser.Parser[provingground.AgdaExpressions.E
                                                  //| xpression] = Parser (Parser (~)^^)
  parseAll(arrow(), "x -> y")                     //> res6: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.7] parsed: Arrow(Token(x),Token(y))
  parseAll(expr, "(x -> y)")                      //> res7: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.9] parsed: Arrow(Token(x),Token(y))
	parseAll(token, "::")                     //> res8: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Token] = [1.3] parsed: Token(::)
  parseAll(expr, "(x : A)")                       //> res9: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpressi
                                                  //| ons.Expression] = [1.8] parsed: TypedVar(x,Token(A))
  parseAll(expr, "a -> b -> c")                   //> res10: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.12] parsed: Arrow(Token(a),Arrow(Token(b),Token(c)))
  parseAll(expr, "(a : A) :-> a")                 //> res11: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.14] parsed: LambdaExp(TypedVar(a,Token(A)),Token(a))
  parseAll(expr, "(a : A) -> a")                  //> res12: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.13] parsed: DepArrow(TypedVar(a,Token(A)),Token(a))
	parseAll(appl(), "a (b -> c)")            //> res13: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.11] parsed: Apply(Token(a),Arrow(Token(b),Token(c)))
	
	parseAll(expr, "AA")                      //> res14: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.3] parsed: Token(AA)
	
	parseAll(expr, "a b")                     //> res15: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.4] parsed: Apply(Token(a),Token(b))
	parseAll(expr, "a b c")                   //> res16: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.6] parsed: Apply(Token(a),Apply(Token(b),Token(c)))
  asTerm("(x : _) :-> x")                         //> res17: Option[provingground.HoTT.Term] = Some((x⟼x))
  parseAll(expr, "(x : _) :-> x")                 //> res18: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Expression] = [1.14] parsed: LambdaExp(TypedVar(x,U),Token(x))
  asTerm("(y : _) :-> (y -> y)")                  //> res19: Option[provingground.HoTT.Term] = Some((y⟼(y⟶y)))
  
  parseAll(eqlty(), "x -> z = f y")               //> res20: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpress
                                                  //| ions.Equality] = [1.13] parsed: Equality(Arrow(Token(x),Token(z)),Apply(Toke
                                                  //| n(f),Token(y)))
  
  val ag = new AgdaPatternParser                  //> ag  : provingground.AgdaExpressions.AgdaPatternParser = provingground.AgdaEx
                                                  //| pressions$AgdaPatternParser@54daea8a
	  
  
  val agda = ag.agdaPtn                           //> agda  : worksheets.AgdaParseTest.ag.Parser[List[String]] = Parser (|)
  
  ag.parseAll(agda, "f_")                         //> res21: worksheets.AgdaParseTest.ag.ParseResult[List[String]] = [1.3] parsed:
                                                  //|  List(f, _)
  
  val ifthenelse = ag.parseAll(agda, "if_then_else_")
                                                  //> ifthenelse  : worksheets.AgdaParseTest.ag.ParseResult[List[String]] = [1.14]
                                                  //|  parsed: List(if, _, then, _, else, _)
 	ifthenelse.get(2).length                  //> res22: Int = 4
 
  val ifparser = new AgdaParse(List(List("for", "_"),List("if","_","then","_", "else","_")))
                                                  //> ifparser  : provingground.AgdaExpressions.AgdaParse = provingground.AgdaExp
                                                  //| ressions$AgdaParse@53854b5f
	ifparser.parseAll(ifparser.expr, "(x : _) :-> x")
                                                  //> res23: worksheets.AgdaParseTest.ifparser.ParseResult[provingground.AgdaExpr
                                                  //| essions.Expression] = [1.14] parsed: LambdaExp(TypedVar(x,U),Token(x))
  def ifp = (x : String) => ifparser.parseAll(ifparser.expr, x)
                                                  //> ifp: => String => worksheets.AgdaParseTest.ifparser.ParseResult[provinggrou
                                                  //| nd.AgdaExpressions.Expression]
  ifp("for x")                                    //> res24: worksheets.AgdaParseTest.ifparser.ParseResult[provingground.AgdaExpr
                                                  //| essions.Expression] = [1.6] parsed: Apply(Token(for_),Token(x))
  
  val iflist = List("if","_","then","_", "else","_")
                                                  //> iflist  : List[String] = List(if, _, then, _, else, _)
  
  parseAll(ptnmatch(iflist),"if x then y else z") //> res25: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpres
                                                  //| sions.Expression] = [1.19] failure: string matching regex `[ \t]+' expected
                                                  //|  but end of source found
                                                  //| 
                                                  //| if x then y else z
                                                  //|                   ^
  parseAll(ptnmatch(List("xxx", "_")),"xxx y")    //> res26: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpres
                                                  //| sions.Expression] = [1.6] parsed: Apply(Token(xxx_),Token(y))
  parseAll(ptnmatch(List("xxx", "_", "zz")),"xxx y zz")
                                                  //> res27: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpres
                                                  //| sions.Expression] = [1.9] failure: string matching regex `[ \t]+' expected 
                                                  //| but end of source found
                                                  //| 
                                                  //| xxx y zz
                                                  //|         ^
  parseAll(ptnmatch(List("xxx", "_", "zz","_")),"xxx y zz w")
                                                  //> res28: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpres
                                                  //| sions.Expression] = [1.11] failure: string matching regex `[ \t]+' expected
                                                  //|  but end of source found
                                                  //| 
                                                  //| xxx y zz w
                                                  //|           ^
  
  parseAll(expr, "if x then y else z")            //> res29: worksheets.AgdaParseTest.parser.ParseResult[provingground.AgdaExpres
                                                  //| sions.Expression] = [1.19] parsed: Apply(Token(if),Apply(Token(x),Apply(Tok
                                                  //| en(then),Apply(Token(y),Apply(Token(else),Token(z))))))
  
  ifp("if x then y else z")                       //> res30: worksheets.AgdaParseTest.ifparser.ParseResult[provingground.AgdaExpr
                                                  //| essions.Expression] = [1.19] parsed: Apply(Token(if),Apply(Token(x),Apply(T
                                                  //| oken(then),Apply(Token(y),Apply(Token(else),Token(z))))))
}