/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *: 26/Sep/2024			    -Se agregaron los procedures correspondientes
 *:-----------------------------------------------------------------------------
 */
package compilador;

import javax.swing.JOptionPane;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean    analizarSemantica = false;
    private String     preAnalisis;
    
    //VARIABLES PARA RESOLVER PROBLEMA DE LA GRAMÁTICA
    private boolean retroceso; //Bandera que indica si se realizó un retroceso 
                               //en el búffer de entrada
    private int ptr;           //Apuntador auxiliar a una localidad del búffer
    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //

    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica

    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        
    }

    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;            
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico

    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    
    //--------------------------------------------------------------------------
    //MÉTODO PARA RETROCEDER EL SÍMBOLO DE preAnalisis EN EL BUFFER DE ENTRADA 
    //A LA POSICIÓN PREVIAMENTE GUARDADA EN ptr
    private void retroceso(){
        cmp.be.setPrt(ptr);
        preAnalisis = cmp.be.preAnalisis.complex;
        retroceso = true;
    }
    
    
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
     private void P() {
        if (preAnalisis.equals("id") || preAnalisis.equals("inicio")) {
            // P -> V C
            V();
            C();

        } else {
            error("[P]: Se esperaba un tipo de dato (id, inicio )"
                    + "No.Linea" + cmp.be.preAnalisis.getNumLinea());
        }

    }

    private void V() {
        if (preAnalisis.equals("id")) {
            //V ID : T V
            emparejar("id");
            emparejar(":");
            T();
            V();
        } else {
            // V → empty
        }

    }

    private void T() {
        if (preAnalisis.equals("entero")) {
            //T -> entero
            emparejar("entero");
        } else if (preAnalisis.equals("real")) {
            // T -> real
            emparejar("real");

        } else if (preAnalisis.equals("caracter")) {
            // T -> caracter
            emparejar("caracter");

        } else {
            error("[T]: Se esperaba un tipo de dato (entero, real, caracter)"
                    + "No.Linea" + cmp.be.preAnalisis.getNumLinea());

        }

    }

    private void C() {
        if (preAnalisis.equals("inicio") || preAnalisis.equals("fin")) {
            //c -> inicio
            emparejar("inicio");
            S();
            emparejar("fin");

        } else {
            error("[C]: Se esperaba inicio , fin" + "No.Linea" + cmp.be.preAnalisis.getNumLinea());
        }

    }

    private void S() {
        if (preAnalisis.equals("id") || preAnalisis.equals("opasig")) {
            //S->id
            //S->opasig
            emparejar("id");
            emparejar("opasig");
            E();
            S();
        } else {
            //S->empty
        }

    }

    private void E() {
        if (preAnalisis.equals("id")) {
            // E -> id
            emparejar("id");
        } else if (preAnalisis.equals("num")) {
            // E -> num
            emparejar("num");
        } else if (preAnalisis.equals("num.num")) {
            // E -> num.num){
            emparejar("num.num");

        } else {
            error("[E]: Se esperaba una expresion(id, num, num.num)."
                    + "No.Linea:" + cmp.be.preAnalisis.numLinea);
        }
    }
}
//                         ↓ procedures java tec ↓
//------------------------------------------------------------------------------
//CODIGO DEL PARSER PREDICTIVO RECURSIVO DE LENGUAJE JAVATEC.

//primeros (clase) = {‘public’}
// Implementado por: Luis Ernesto Barranco (21130876)
private void clase(){
            if (preAnalisis.equals("public")){
		/* clase → public class  id  {   declaraciones    declaraciones_metodos    metodo_principal }*/
                emparejar("public");
                emparejar("class");
                emparejar("id");
                emparejar("{");
                declaraciones();
                declaraciones_metodos();
                metodo_principal();
                emparejar("}");
            }else{
                error("[clase] Error al iniciar la clase "+cmp.be.preAnalisis.numLinea);
            }
    }
//-----------------------------------------------------------------
                          //lista_identificadores
//procedure lista_identificadores
//PRIMEROS(lista_identificadores) = {"id"}
//
public void lista_identificadores(){
      if(preAnalisis.equals("id")){
          emparejar("id");
          dimension();
          lista_identificadores_prima();
     }else{
           error("[lista_identicadores] Error en lista identifiacores "+ cmp.be.preAnalisis.numLinea);
   
   }
}


//-----------------------------------------------------------------
// Código en java
// IMPLEMENTADO POR: Edgar Manuel Carrillo Muruato 21130864
// Primeros lista_identificadores_prima = { , } U { empty }

private void lista_identificadores_prima(){

    if( preAnalisis.equals( "," ) ){

	// lista_identificadores’ -> , id  dimension  lista_identificadores’
	emparejar ( "," );
	emparejar ( "id" );
	dimension ( );
	lista_identificadores_prima ( );

    }else{
	// lista_identificadores’ -> empty 
    }
}
//--------------------------------------------------------------------
                         //declaraciones
//PRIMEROS (declaraciones) = {'public'} U {'ϵ'}
// Diseñado por :  Paulina Jaqueline Castañeda Villalobos(21130850)
// declaraciones -> public static tipo lista_identificadores ; declaraciones

    private void declaraciones(){
       if(preAnalisis.equals("public") ){
          //declaraciones -> public static tipo lista_identificadores ; declaraciones
           emparejar("public");
           emparejar("static");
           tipo();
           lista_identificadores();
           emparejar(";");
           declaraciones();
  }else{
         //declaraciones -> empty
    }
}
//------------------------------------------------------------------------
// Codigo implementado en JAVA
//  Primeros ( tipo ) = First(tipo_estandar) = { entero,  float,  string }
//
 //Implementado por: Gael Costilla Garcia (21130923) 
    private void tipo(){
        if( preAnalisis.equals( "entero" | "float" | "string" ){
            // tipo -> tipo_estandar
            tipo_estandar();
        }
        else{
           error( " [tipo] tipo de dato no reconocido (int, float, string) " );
        }
            
    }
//------------------------------------------------------------------------

// CODIGO EN JAVA;
// Implementado por Jose Eduardo Gijon Mora  (21130883)
// PRIMEROS (tipo_estandar) = {'int'} U {'float'} U {'string'}
private void tipo_estandar() {

        if (preAnalisis.equals("int")) {
            // tipo_estandar -> int
            emparejar("int");

        } else if (preAnalisis.equals("float")) {
            // tipo_estandar -> float
            emparejar("float");

        } else if (preAnalisis.equals("string")) {
            // tipo_estandar -> string
            emparejar("string");

        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato estándar. Línea: "+ cmp.be.preAnalisis.numLinea);
        }
    }
//-------------------------------------------------------------------------------

//-------------------------------------------------------------------------------

    //PROCEDURE lista_parametros 
    //Primeros ( lista_parametros ) = { int, float, string } U { empty } 
    //Implementado por: Rodrigo Macias Ruiz (21131531)
    private void lista_parametros(){
        if ( preAnalisis.equals ( “int” ) || preAnalisis.equals ( “float” ) || preAnalisis.equals ( "string" ) ) {
             //lista_parametros →  tipo  id  dimension  lista_parametros’  | ϵ
              tipo ();
              emparejar ( “id” );
              dimension ();
              lista_parametros_prima ();
        } else {
             // lista_parametros -> empty
        }
    }




//-------------------------------------------------------------------
//Procedure Proposicion’
//
// PRIMERO (proposicion') = { [ } U { opasig } U { ( } U { empty };
//
// Diseñado por: Manuel Mijares Lara Hola Noloh :), noloh deja de usar el cel
    private void proposicion_prima(){
        if ( preAnalisis.equals ( "[" ) ){
            //proposicion' -> [expresion] opasig expresion
            emparejar ( "[" );
            //expresion();
            emparejar ( "]" );
            emparejar ( "opasig" );
            //expresion();
        }else if ( preAnalisis.equals ( "opasig" ) ){
            //proposicion' -> opasig expresion
            emparejar ( "opasig" );
            //expresion();
        }else if ( preAnalisis.equals( "(" ) ){
            //proposicion' -> (lista_expresiones)
            emparejar ( "(" );
            //lista_expresiones();
            emparejar ( ")" );
        }else{
            //proposicion' -> empty
        }
    }

//Diseñado por: Sergio Antonio López Delgado (21130847)
//Primero (lista_parametros) = {tipo},  {empty}

private void lista_parametros(){
        if ( preAnalisis.equals ( “tipo” ) ){
            //lista_parametros -> tipo id dimension lista_parametros
            emparejar ( “tipo” );
	    emparejar ( “id” );
	    emparejar ( “dimension” );
	    emparejar ( “lista_parametros” );
        }else{
            //lista_parametros -> empty
        }
    }

//-----------------------------------------------------------------------------
//Diseñado por: José Alejandro Martínez Escobedo - 19130939
//PROCEDURE proposicion_compuesta

private void proposicion_compuesta(){
	if(preAnalisis.equals("id"){
		emparejar("id");
	}else{
		error("[proposicion_compuesta] Se esperaba (id): " + " - Linea: " + //cmp.be.preAnalisis.numLinea)
	}
}





//-----------------------------------------------------------------------------
// Diseñado por: Layla Vanessa González Martínez 21130868
// PROCEDURE dimension
// Primeros (dimension) = {[} U {empty}

private void dimension() {
   if ( preAnalisis.equals( "id" ) ) {
      // dimension -> [ num ]
      emparejar ( “[“ );
      emparejar ( “num” );
      emparejar ( “]” );
   }else{
      // dimension -> empty
   }
}


//------------------------------------------------------------------------------
    // PROCEDURE lista_expresiones'
    // PRIMERO(lista_expresiones') = { ',' } U { 'empty' }
    // Implementado por: Diego Muñoz Rede (21130893)
    private void lista_expresiones_prima()
    {
        if( preAnalisis.equals( "," ) )
        {
            // lista_expresiones' -> , expresion lista_expresiones'
            emparejar( "," );
            expresion();
            lista_expresiones_prima();
        }
    else{ /*lista_expresiones' -> empty*/ }
    }



//------------------------------------------------------------------------------
//PRIMEROS(corchetes) = { [ } U { ϵ }
//Implementado por: Jesus Emmanuel Llamas Hernandez - 21130904
private void corchetes(){
   if (preAnalisis.equals( "[" )) {
	// corchetes -> []
	emparejar ( "[" );
	emparejar ( "]" );
   }else{
	// corchetes -> ϵ
   }
}


//------------------------------------------------------------------------------
//			IMPLEMENTACIÓN
//Implementado por :  Paulina Jaqueline Castañeda Villalobos (21130850)
//PRIMEROS (declaraciones) = {'public'} U {'ϵ'}
private void declaraciones( ){
    if( preAnalisis.equals( "public" ) ){
        //declaraciones -> public static tipo lista_identificadores ; declaraciones
        emparejar( "public" );
        emparejar( "static" );
        tipo( );
        lista_identificadores( );
        emparejar( ";" );
        declaraciones( );
    }else{
        //declaraciones -> ϵ
    }
}

//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
                           //encab metodo
//------------------------------------------------------------------------------
 // Código implementado en JAVA
// IMPLEMENTADO POR: Diana Laura Juarez Cordova
Procedure (metodo_principal)
Primeros (metodo_principal) = 
public static void metodo_principal() {
	if (preAnalisis.equals("public"){
	      metodo_principal();
	      metodo_principla();
	}else {
	  //empty
	}
}

//------------------------------------------------------------------------------
// CODIGO tipo_metodo
// PRIMERO ( tipo_metodo )   = { ‘void’ , ‘int’ , ‘float’ , ‘string’ }
// Implementado por: Marcos Juárez ( 21130852 )
private void tipo_metodo () {
        if ( preAnalisis.equals ( "void" ) ) {
            // tipo_metodo -> void
            emparejar ( "void" );
        }else if(preAnalisis.equals("int")|| 
                   preAnalisis.equals("float") || 
                   preAnalisis.equals("string")) {
            // tipo_metodo -> tipo_estandar corchetes
            tipo_estandar();
            corchetes();
        }else{
            error ( "[tipo_metodo] - Tipo de dato incorrecto ( int, float, string ). " + 
                    "Linea: " + cmp.be.preAnalisis.numLinea);
        }
}

//-------------------------------------------------------------------------------
// PROCEDURE termino
// PRIMEROS ( termino )  = PRIMEROS ( factor ) 
//                       = { id, num, num.num, ( }
// Implementando por:  Juan Fernando Vaquera Sanchez 21130869
    
    private void termino(){
        
        if ( 
             
             preAnalisis.equals ( "id" ) ||  preAnalisis.equals ( "num" ) ||  
             preAnalisis.equals ( "num.num" ) ||  preAnalisis.equals ( "(" ) 
            
            ){
            
            // termino -> factor termino'
            factor();
            termino_prima();
            
        } else{
            
            error("[termino] Falto la definicion del factor al inicio del termino. Linea: " + 
            cmp.be.preAnalisis.numLinea);
            
        }
                
    }

//------------------------------------------------------------------------------
                                  // dimension
//------------------------------------------------------------------------------
                                 // delclaraciones_metodos
//Implementado por: ANA SOFIA GONZALEZ VALERIO
private void declaraciones_metodos() {
	if (preAnalisis.equals("public"){
	      declaración_metodo();
	      declaraciones_metodos();
	}else {
	  //empty
	}
}
//------------------------------------------------------------------------------
                                // declaracion_metodo
//Implementado por: VALERY ARACELI GUERRERO RODRIGUEZ (21130925) 
private void declaracion_metodo(){
    if(preAnalisis.equals ("public static")||(" { ")){
	encab_metodo()
	declaracion_()	
    }
    else {
	error("[declaracion_metodo] El programa debe iniciar con"
		 + "declaracion de variable con la palabra reservada"
		 + "(public static) ({")
}

}

//------------------------------------------------------------------------------
                                // encab metodo
// Implementado por: Alejandro Huerta Reyna 21130857

private void encab_metodo() {
  if ( preAnalisis.equals (   "public"   ) )  {

    // encab_metodo	→  public static  tipo_metodo  id( lista_parametros ) 
      emparejar( "public" );
      emparejar( "static" );
      tipo_metodo();
      emparejar("id");
      emparejar("(");
      lista_parametros();
      emparejar(")");
  } else {
    error ( "encab_metodo - Error de sintaxis " 
    + "Linea: " + cmp.be.preAnalisis.numLinea );
  }
}

//------------------------------------------------------------------------------
                               // corchetes
//------------------------------------------------------------------------------
                               // lista_parametros
//------------------------------------------------------------------------------
                              // lista_parametros_prima
//------------------------------------------------------------------------------
                             // proposicion_compuesta
//------------------------------------------------------------------------------
                            // proposiciones_optativas
// Diseñado por: Rodrigo Mazuca Ramirez
// Procedure proposiciones-optativas
// PRIMERO ( proposiciones-optativas) = RRIMERO ( lista_proposiciones )
// PRIMERO 
// PRIMERO ( prioposicion ) = PRIMERO (proposicion’)
// PRIMERO (proposicion) = { id , if, while, {  } 

private void proposiciones_optativas (){
	if ( preAnalisis.equals("id", "if", "while", "{" )) {
		lista_proposiciones()
	} else {
		//Proposicion_optatica ->empty
	}

}

//------------------------------------------------------------------------------
                            //lista_proposiciones
//------------------------------------------------------------------------------
                            // propocicion
// Implementado por: Humberto Medina Santos (21130862)
public void proposicion() {
	if ( preAnalisis.equals("id") {
			// proposicion -> id proposicion' ; 
			emparejar("id");
			proposicion_prima();
			emparejar(";");
	} else if ( preAnalisis.equals("{") {
			// proposicion -> proposicion_compuesta
			proposicion_compuesta();	
	} else if ( preAnalisis.equals("if") {
			// proposicion -> if ( expresión ) proposición else proposición
			emparejar("if");
			emparejar("(");
			expresion();
			emparejar(")");
			proposicion();
			emparejar("else");
			proposicion();
	} else if preAnalisis.equals("while") {
			// proposicion -> while ( expresión ) proposición 
			emparejar("while");
			emparejar("(");
			expresion();
			emparejar(")");	
			proposicion();
	} else {
			error("[proposicion] Error en la proposicion Linea:" + cmp.be.preAnalisis.numLinea);
	}

}
//------------------------------------------------------------------------------
                           //proposicion_prima
//------------------------------------------------------------------------------
                           //proposicion_metodo
//------------------------------------------------------------------------------
                          // lista_expresiones
//------------------------------------------------------------------------------
                          // lista_expresiones_prima
//------------------------------------------------------------------------------
                          // expresion
//------------------------------------------------------------------------------
                         // expresion_prima
//------------------------------------------------------------------------------
//PROCEDURE expresion_simple
//Primero (expresion_simple) = PRIMEROS(termino)
//			     = {id,num, num.num,(}
//Implementado por: Miriam Alicia Sanchez Cervantes (21130882)

private void expresion_simple (){
        if(preAnalisis.equals("id") || preAnalisis.equals("num" ) || preAnalisis.equals("num.num") || preAnalisis.equals("(")){
            
	    termino();
            expresion_simple_prima();
            
            
        }else{
            error("[expresion_simple]Tiene que empezar con (id, num, num.num, ( ). Linea: " + cmp.be.preAnalisis.numLinea);
        }
            
    }
//------------------------------------------------------------------------------
// Código implementado en Java
// PRIMEROS (expresion_simple_prima) = {'opsuma' } U { 'empty' }
// Implementado por : María Fernanda Torres Herrera (21130859)

private void expresion_simple_prima () {
	if ( preAnalisis.equals( "opsuma" ) ) {
		// expresion_simple_prima -> opsuma termino expresion_simple_prima
		emparejar ( "opsuma" );
		termino();
		expresion_simple_prima();
	}else{
		// expresion_simple_prima -> empty
	}
}
//—----------------------------------------------------------------------------
                                  //termino
//—----------------------------------------------------------------------------
//Implementado por: Luis Alejandro Vazquez Saucedo  
//PROCEDURE término’
//PRIMERO(termino’) = {opmult}  

public void termino_prima(){
  if(preAnalisis.equals("opmult") ){
       //termino’→ opmult
       emparejar("opmult");
       factor();
       termino_prima(); 
    }
  else{
       error("[termino']: Se esperaba(opmult)" + "Linea:" + 
            cmp.be.preAnalisis.numLinea);
    }
}
//—----------------------------------------------------------------------------
                           //factor

//—-----------------------------------------------------------------------------
                           //factor_prima
    //PROCEDURE FACTOR'
    // PRIMERO factor’  = { ( } U  { empty }
    //Implementado por: Leonardo Zavala (21130874)
        private void factor_prima () {
        if ( preAnalisis.equals ( "(" ) ) {
           // factor' -> ( lista_expreisones )
            emparejar ("(");
           lista_expresiones();
            emparejar(")");

        } else {
            // factor' -> empty
        }
    }
//------------------------------------------------------------------------------
}    
}
//------------------------------------------------------------------------------
//::
