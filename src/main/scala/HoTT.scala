package provingGround

import scala.language.implicitConversions 
import scala.util._
import scala.language.existentials
//import scala.reflect.runtime.universe._


// T
object HoTTouter{
    trait AbsObj {
        val typ: Typ[AbsObj]
    }
    
    trait Typ[+U <: AbsObj] extends AbsObj{
        type Obj = U
        
        def symbObj[A](name: A): Obj
        
        def ::[A](name:A) = symbObj(name) 
    }
    
    type EffectiveTyp[U <: AbsObj] = Typ[U]

    trait Symbolic[A]{
      val name: A
      override def toString = name.toString
    }
    
    case class SymbObj[A, U<: AbsObj](name: A, typ: Typ[U]) extends AbsObj with Symbolic[A] 
    
    
    case class SymbTyp[A, U<:AbsObj](name: A, univ: Univ[U]) extends Typ[U] with Symbolic[A]{
      lazy val typ = univ
      
      def symbObj[A](name: A) = this.symbObj(name)
    }

    class LogicalTyp extends Typ[AbsObj]{
      
      lazy val typ = LogicalUniv
      
      def symbObj[A](name: A): AbsObj with Symbolic[A] = SymbObj(name, this)
      
      def -->[V <: AbsObj](that: Typ[V]) = FuncTyp(this, that)
    }
    
    trait Univ[U<:AbsObj] extends Typ[Typ[U]]{
      def symbObj[A](name: A) = SymbTyp(name, this)
    }
    
    case class NextUniv[U<: AbsObj]() extends Univ[U]{
      lazy val typ = NextUniv[Typ[U]]
      
    }
    
    object LogicalUniv extends Univ[AbsObj]{
      lazy val typ = NextUniv[AbsObj]
      
      override def toString ="__"
    }
    
    val __ = LogicalUniv
    
    
    case class PairTyp[U<: AbsObj, V <: AbsObj](first: Typ[U], second: Typ[V]) extends 
						Typ[AbsPair[U, V]]{
      

		lazy val typ = PairTyp(first, second)

			
			// The name is lost
		def symbObj[A](name: A): AbsPair[U, V] = PairObj(first.symbObj(name), second.symbObj(name))

   
			}	
    
    case class PairObj[U <: AbsObj, V <: AbsObj](val fst: U, val scnd: V) extends AbsPair[U, V]{
    	lazy val typ = PairTyp(fst.typ, scnd.typ)
					}

	trait AbsPair[U<: AbsObj, V <: AbsObj] extends AbsObj
    
    case class FuncTyp[U<: AbsObj](dom: LogicalTyp, codom: Typ[U]) extends LogicalTyp{
	  override def symbObj[A](name: A) = FuncSymb(name, dom, codom)
	  
	  override def toString = dom.toString + " -> " + codom.toString
	}
    
    trait FuncObj[U <: AbsObj] extends AbsObj{
	  val dom: LogicalTyp 
	  val codom: Typ[U]
	  
	  lazy val typ = FuncTyp(dom, codom)
	  
	  def act(arg: AbsObj): Option[U]
	  
	  def apply(arg: AbsObj) = act(arg)
	  
	}

	trait FormalFunc[U <: AbsObj] extends FuncObj[AbsObj]{
	  def act(arg: AbsObj) = if (arg.typ == dom) Some(FormalApplication(this, arg)) else None
	}
	
    case class FuncSymb[A, U<: AbsObj](name: A, dom: LogicalTyp, codom: Typ[U]) extends FormalFunc[U] with Symbolic[A]
	
    private case class FormalApplication[U<: AbsObj](func: FuncObj[U], arg: AbsObj) extends AbsObj{
      lazy val typ = func.codom
      
      override def toString = func.toString + "("+ arg.toString +")"
    }
	
	case class FnSym[F, A](func: F, arg: A){
      override def toString = func.toString+"("+arg.toString+")"
    }
	
	case class FuncDefn[U <: AbsObj](func: AbsObj => U, dom: LogicalTyp, codom: Typ[U]) extends FuncObj[U]{
	  def act(arg: AbsObj) = if (arg.typ == dom) Some(func(arg)) else None
	}
	
	case class Lambda[U<: AbsObj, V <: AbsObj](variable: U, value : V) extends AbsObj{
	  lazy val typ = (variable.typ.asInstanceOf[LogicalTyp]) --> value.typ
	}
			
	def lambda[U<: AbsObj, V <: AbsObj](variable: U)(value : V) = Lambda(variable, value)
	
	type TypFamily[U <: AbsObj] = FuncObj[Typ[U]]
	
	case class PiTyp[U <: AbsObj](fibers: TypFamily[U]) extends LogicalTyp
	
	case class SigmaTyp[U <: AbsObj](fibers: TypFamily[U]) extends LogicalTyp
	
	trait DepFuncObj[U <: AbsObj] extends AbsObj{
	  val fibers: TypFamily[U] 
	   
	  
	  lazy val typ = PiTyp(fibers)
	  
	  def act(arg: AbsObj): Option[U]
	  
	  def apply(arg: AbsObj) = act(arg)
	  
	}
	
	trait FormalDepFunc[U<: AbsObj] extends DepFuncObj[AbsObj]{
	  def act(arg: AbsObj) = if (arg.typ == fibers.dom) Some(FormalDepApplication(this, arg)) else None
	}
	
	case class DepFuncSym[A, U <: AbsObj](name: A, fibers: TypFamily[AbsObj]) extends FormalDepFunc[U] with Symbolic[A]
	
	private case class FormalDepApplication[U<: AbsObj](func: DepFuncObj[U], arg: AbsObj) extends AbsObj{
	  val typFamily = func.fibers
      lazy val typ : Typ[U] = (typFamily(arg)).get
      
      override def toString = func.toString + "("+ arg.toString +")"
    }
	
	case class IdentityTyp[U <: AbsObj](dom: Typ[U], lhs: AbsObj, rhs: AbsObj) extends LogicalTyp
	
	
	
	
	val x = 'x' :: __
	
	val y = "y" :: x
}















object HoTT{
  
   
    trait AbsObj {
      val typ: Typ[AbsObj]
      
      def as[U <: AbsObj](that: Typ[U]) = {assert (typ==that); this.asInstanceOf[that.Obj]}

	  def as(that: LogicalTyp) = {assert (typ==that); this.asInstanceOf[that.LogicalObj]}
     }
         

    trait Elem[+A]{def elem: A}
    
    implicit def baseElem[A](b: Elem[A]): A = b.elem
    
     
    
	
 	   
//	implicit class AsLogicalTyp[U<: AbsObj](typ: Typ[U]) extends LogicalTyp{
//		implicit class AsLogicalObj(obj: Obj) extends LogicalObj
//		} 

	trait Symbolic[A]{
	    val name: A
//	    val tpe: 
	}

    trait EffectiveTyp[+U <: AbsObj] extends Typ[U]{self =>
      /** Object given by its name */
      def symbObj[A](id: A): Obj   
      
      implicit def asObj(sym: Symbol) = symbObj(sym)

      def ::(sym: Symbol) = symbObj(sym)
    }
    
    case class FnSym[F, A](func: F, arg: A){
      override def toString = func.toString+"("+arg.toString+")"
    }
    
    /** Any type that exists for a reason better than being a universe */
    trait LogicalTyp extends EffectiveTyp[AbsObj]{
    	
    	lazy val typ: Typ[AbsObj] = LogicalUniv

		def -->[V <: AbsObj](that: EffectiveTyp[V]) = FuncTyp(this, that)

		case class SymbObj[A](name: A) extends LogicalObj with Symbolic[A]{
			override def toString = name.toString + " : " + typ.toString
		}
			
		def symbObj[A](id: A) : LogicalObj = SymbObj(id)
			
		case class ApplObj[F, A](func: F, arg: A) extends LogicalObj{
			override def toString = func.toString+"("+arg.toString+")"
		}

		def applObj[F, A](f: F, a: A) = ApplObj(f,a)
									
		class LogicalObj extends Obj{
			def elem : AbsObj = this		
			}

    }

		case class PairTyp[U<: AbsObj, V <: AbsObj](first: EffectiveTyp[U], second: EffectiveTyp[V]) extends 
						EffectiveTyp[AbsPair[U, V]]{

			lazy val typ = PairTyp(first, second)

			class PairObj(val fst: first.Obj, val scnd: second.Obj) extends Obj{
					def elem = ObjPair(fst.elem, scnd.elem)
					}

			def symbObj[A](id: A): Obj = new PairObj(first.symbObj(id), second.symbObj(id))

   
			}	

		trait AbsPair[U<: AbsObj, V <: AbsObj] extends AbsObj

		case class ObjPair[U <: AbsObj, V <: AbsObj](fst: U, scnd: V) extends AbsPair[U, V]{
			lazy val typ = PairTyp(fst.typ.asInstanceOf[EffectiveTyp[U]], scnd.typ.asInstanceOf[EffectiveTyp[V]])
			}

		implicit def BinFunc[U<: AbsObj, V <: AbsObj, W <: AbsObj](first: EffectiveTyp[U], 
																															second: EffectiveTyp[V],
                                                              target: EffectiveTyp[W])(
																																defn : (first.Obj, second.Obj) => target.Obj) = {
						val fn = FuncTyp(PairTyp(first, second), target)
						val castDefn: fn.dom.Obj => fn.codom.Obj = {
								arg: fn.dom.Obj => defn(arg.asInstanceOf[(first.Obj, second.Obj)]._1,
																													arg.asInstanceOf[(first.Obj, second.Obj)]._2).as(fn.codom)}
						fn.Defn(castDefn)
						}


		trait Univ[+U<: AbsObj] extends EffectiveTyp[Typ[U]]{
			trait TypObj extends Obj with Typ[U]{
				def elem: Typ[U] = this
			}

			} 
	
     
    
    object LogicalUniv extends Typ[LogicalTyp] with Univ[AbsObj]{
      class TypObj extends Obj with LogicalTyp{
        def elem: LogicalTyp = this
        override lazy val typ = LogicalUniv
      }
      
      case class SymbObj[A](name: A) extends TypObj with Symbolic[A]{
			override def toString = name.toString + ":" + typ.toString
		}

      def symbObj[A](name: A) = SymbObj(name)
      
      lazy val typ = NextUniv[Typ[AbsObj], Typ[Typ[AbsObj]]]
      
      implicit class InUniv(tp: LogicalTyp) extends TypObj
      
      override def toString = " _ "
    }
    
	val __ = LogicalUniv

	val A = 'a :: __

	'b :: A

	case class NextUniv[U <: AbsObj, V <: Typ[U]]() extends Typ[V] with Univ[U]{
		lazy val typ = NextUniv[V, Typ[V]]
		
		implicit class InUniv(tp: Typ[U]) extends TypObj
			
		case class SymbObj[A](name: A) extends TypObj

		def symbObj[A](name: A) = SymbObj(name)
    }
    

    
    trait Typ[+U <: AbsObj] extends AbsObj {self =>
           
      val typ: Typ[AbsObj]   
   

      

      trait Obj extends AbsObj with Elem[U]{
        def elem : U 
        
        lazy val typ = self
        
        val obj = this
        
        
        val id = Identity(this, this)
        
        case object refl extends id.LogicalObj
        
        def =:=(that: Obj) = Identity(this, that)
        
        def =:=(that:AbsObj) = eql(typ)(this)(that)		 
      }
      
   
      
      def :::(obj: AbsObj) = obj.as(this)
      
      def :::(tryobj: Try[AbsObj])=Try(tryobj.get as this)
      
	  def ::(obj: AbsObj) = obj.as(this).elem

      case class Identity(left: Obj, right: Obj) extends LogicalTyp      
    }

	case class Logical[T](implicit tag: scala.reflect.runtime.universe.TypeTag[T]) extends LogicalTyp{
		implicit class AsLogicalObj(ob: T) extends LogicalObj
		
		def ::(t: T) = AsLogicalObj(t)
		} 

	
    implicit def me(arg: AbsObj): arg.typ.Obj = arg ::: arg.typ
    
    
    
    def eql(typ: Typ[AbsObj])(left: AbsObj)(right: AbsObj) = typ.Identity(left as typ, right as typ) 
    
    3 :: Logical[Long]
    
    trait AbsFunc[+U <: AbsObj] extends AbsObj{
      val domain: Typ[U];
      def apply(obj: AbsObj): AbsObj
    }
    
	
    case class IndexTyp(tp: Typ[AbsObj]){
      case class ForSection[+U <: AbsObj](section: tp.Obj => EffectiveTyp[U]) extends LogicalTyp{
        case class Index(arg: tp.Obj){
          val codomain = section(arg)

          
          case class DepPair(value: codomain.Obj) extends DepPairTyp.LogicalObj
        }
        
        case object DepFnTyp extends LogicalTyp{
	
        	trait FunctionalObj extends LogicalObj with AbsFunc[AbsObj]{
        		def act(arg: tp.Obj) = {
        			val domain = Index(arg)

							domain.codomain.symbObj(FnSym(this, arg))
        		}
          
        		def apply(arg:tp.Obj) = act(arg)
          
        		def apply(arg: AbsObj) = act(arg.as(tp))
        	}
        	
        }
        
        case object DepPairTyp extends LogicalTyp{
//          def ::(indx: tp.Obj, obj: AbsObj) = {
//            val indxTyp = Index(indx)
//            indxTyp.DepPair(obj as indxTyp.codomain)
//          }
        
        }
      }            
    }
    
    implicit def indexTyp(tp: Typ[AbsObj]) = IndexTyp(tp) 
    
    def Pi[U<: AbsObj](base: Typ[AbsObj])(section:base.Obj => EffectiveTyp[U]) ={
    	val indx = IndexTyp(base)
    	val sect = (x: indx.tp.Obj) => section(x as base)
    	indx.ForSection(sect).DepFnTyp
      }
    
    def Sigma[U <: AbsObj](base: Typ[AbsObj])(section:base.Obj => EffectiveTyp[U]) ={
    	val indx = IndexTyp(base)
    	val sect = (x: indx.tp.Obj) => section(x as base)
    	indx.ForSection(sect).DepPairTyp
      }
 
    
	case class FuncTyp[U<: AbsObj, V <: AbsObj](dom: Typ[U], codom: EffectiveTyp[V]) extends LogicalTyp{
	  //  case class Appl(f: Obj, arg: dom.Obj) extends codom.LogicalObj{
  	//    override val freeVars = arg.freeVars ++ f.freeVars
    //    }     
      
      override def toString = dom.toString+" -> "+codom.toString
      
	  case class LogicalFunction[A](sym: A) extends FunctionalObj{
        def act(arg: dom.Obj) : codom.Obj = codom.symbObj(FnSym(sym, arg))
      }
      
	  override def symbObj[A](sym: A) = LogicalFunction(sym)

      trait FunctionalObj extends LogicalObj  with AbsFunc[AbsObj]{
        val domain = dom
        
        def act(arg: dom.Obj) : codom.Obj  
        
        def apply(arg: dom.Obj): codom.Obj  =  act(arg)
        
        def apply(arg: AbsObj): codom.Obj = act(arg.as(dom))
        
        def apply(tryarg: Try[AbsObj]) = Try(act(tryarg.get as dom))
        
       
      }
       
      implicit class Defn(defn: dom.Obj => codom.Obj) extends FunctionalObj{ 
          def act(arg: dom.Obj) = defn(arg)
      }
      
      case class Lambda(variable: dom.Obj, value: AbsObj) extends LogicalObj
       
    }
     

	def lambda[U<: AbsObj, V <: AbsObj](dom: Typ[U], codom: EffectiveTyp[V])(variable: dom.Obj, value: codom.Obj) ={
					val fnTyp = FuncTyp(dom, codom)
					fnTyp.Lambda(variable.asInstanceOf[fnTyp.dom.Obj], value as fnTyp.codom)
				}

 

 
	def mk(a: Typ[AbsObj], b: Typ[AbsObj]): a.Obj => b.Obj = {assert(a==b); (x:a.Obj) => x.asInstanceOf[b.Obj]}

  //  val idtest = ('n :: Nat) --> ('n :: Nat) 
	
	def nextChar(s: Set[Char]) = if (s.isEmpty) 'a' else (s.max + 1).toChar
  
	def usedChars(s: Set[AbsObj]): Set[Char] = {
	    def charOpt (obj:AbsObj) : Option[Char] = obj match {
	      case sym: Symbolic[_] => Some(Try(sym.name.asInstanceOf[Char]).toOption).flatten
	      case _ => None
	    }
	    
	    
	    s collect (Function.unlift(charOpt _))
	}
	
  object Nat{
    case object ZeroTyp extends LogicalTyp
    
    case object OneTyp extends LogicalTyp
    
    case object star extends OneTyp.LogicalObj
    
    trait ConstructorDomain{
      val dom: LogicalTyp => LogicalTyp 
      
      def apply(that: LogicalTyp) = dom(that)
    }
    
    
    case class ConstContrDom(typ: LogicalTyp) extends ConstructorDomain{
      val dom = (that: LogicalTyp) => typ
    }
    
    case class ToThis(domdom: ConstructorDomain) extends ConstructorDomain{
      val dom = (that: LogicalTyp) =>  domdom.dom(that) --> that
    }
    
    case class InductiveTyp(constructors: Map[Symbol, ConstructorDomain]) extends LogicalTyp{
      val constrs = constructors map (_._2)
      
      def rec(that: LogicalTyp) = {}
    }
    

    
    case object Nat extends LogicalTyp{
      case class Rec(that: LogicalTyp){
        val fnTyp = FuncTyp(Nat, that)
        type domTyp = fnTyp.dom.Obj
        val tgt = Nat --> (that --> that)
//        case class rec(base: that.Obj, step: tgt.Obj) extends fnTyp.LogicalFunction{
//          val atZero = this(zero.as(fnTyp.dom)).as(that)
//          val baseIdtyp = that.Identity(atZero, base)
//          case object baseId extends baseIdtyp.LogicalObj
//        }
        
      }
      
//      def rec[U <: AbsObj, V<: AbsObj](base: AbsObj)(step: FuncTyp[U, V]) = step.typ match {
//        case FuncTyp(Nat, FuncTyp(that : LogicalTyp, other)) if that == other =>
//        val R = Rec(that)
//        val stp = step.asInstanceOf[R.tgt.Obj]
//        R.rec(base as R.that, stp)
//      }
    }
    
    case object zero extends Nat.LogicalObj 
    
    case class succ(n: Nat.Obj) extends Nat.LogicalObj
    
    val one = succ(zero)
 
  }
    
  
}
