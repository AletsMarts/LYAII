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
    private boolean analizarSemantica = false;
    private String preAnalisis;
    //variables para resolver el problema de la gramatica
    private boolean retroceso; //bandera que indica si se realizo un retroceso en el buffer de entrada
    private int ptr; //apuntador auxiliar a una localidad del buffer.

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
        clase();
    }

    //--------------------------------------------------------------------------
    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;
        } else {
            errorEmparejar(t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
    private void errorEmparejar(String _token, String _lexema, int numLinea) {
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
        msjError += " se encontró " + (_lexema.equals("$") ? "fin de archivo" : _lexema)
                + ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico
    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    private void retroceso() {
        cmp.be.setPrt(ptr);
        preAnalisis = cmp.be.preAnalisis.complex;
        retroceso = true;
    }
    // Fin de error
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------

    private void clase() {
        if (preAnalisis.equals("public")) {
            // clase → public class id { declaraciones declaraciones_metodos metodo_principal }
            emparejar("public");
            emparejar("class");
            emparejar("id");
            emparejar("{");
            declaraciones();
            declaraciones_metodos();
            metodo_principal();
            emparejar("}");
        } else {
            error("[clase] Error: Se esperaba 'public' al iniciar la clase " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------------------------------
    private void lista_identificadores() {
        if (preAnalisis.equals("id")) {
            // lista_identificadores → id dimension lista_identificadores’
            emparejar("id");
            dimension();
            lista_identificadores_prima();
        } else {
            error("[lista_identificadores] Error: Se esperaba un identificador en la línea " + cmp.be.preAnalisis.numLinea);
        }
    }
//------------------------------------------------------------------------------

    private void lista_identificadores_prima() {
        if (preAnalisis.equals(",")) {
            // lista_identificadores’ → , id dimension lista_identificadores’
            emparejar(",");
            emparejar("id");
            dimension();
            lista_identificadores_prima();
        }
        // lista_identificadores’ → ϵ (no se hace nada)
    }

//------------------------------------------------------------------------------
    private void declaraciones() {
        retroceso = false;
        if (preAnalisis.equals("public")) {
            // declaraciones → public static tipo lista_identificadores ; declaraciones
            ptr = cmp.be.getPrt();
            emparejar("public");
            emparejar("static");
            tipo();
            if (!retroceso) {//si no hubo retroceso continuamos...
                lista_identificadores();

                if (preAnalisis.equals(";")) {

                    //si se trata de una sentencia de feclaracion de variables
                    emparejar(";");
                    declaraciones();

                } else {
                    retroceso();
                }
            }
        } else {
            //declaraciones -> empty
        }
    }

    //------------------------------------------------------------------------------  
    private void tipo() {
        //tipo → tipo_estandar
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            tipo_estandar();

        } else if (preAnalisis.equals("void")) {
            //si el tipo es void corresponde a la declaracion de un metodo.
            retroceso();

        } else {
            error("[tipo] tipo de dato no reconocida(int, float, string Linea:)" + cmp.be.preAnalisis.numLinea);

        }

    }
//------------------------------------------------------------------------------

    private void tipo_estandar() {
        //tipo_estandar → int | float | string
        if (preAnalisis.equals("int")) {
            emparejar("int");
        } else if (preAnalisis.equals("float")) {
            emparejar("float");
        } else if (preAnalisis.equals("string")) {
            emparejar("string");
        } else {
            error("[tipo_estandar] Error: Se esperaba 'int', 'float' o 'string' en la línea " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------------------------------
    private void dimension() {
        if (preAnalisis.equals("[")) {
            // dimension → [ num ] | ϵ
            emparejar("[");
            emparejar("num");
            emparejar("]");
        }
        // Caso ϵ: no se hace nada si no hay corchete.
    }

//------------------------------------------------------------------------------
    private void declaraciones_metodos() {
        if (preAnalisis.equals("public")) {
            //declaraciones_metodos → declaración_metodo declaraciones_metodos | ϵ
            declaracion_metodo();
            declaraciones_metodos();
        }
        // Caso ϵ: no se hace nada si no hay más declaraciones de métodos.
    }

//------------------------------------------------------------------------------
    private void declaracion_metodo() {
        if (preAnalisis.equals("public")) {
            //declaracion_metodo → encab_metodo proposición_compuesta
            encab_metodo();
            proposicion_compuesta();
        } else {
            error("[declaración_metodo] Error: Se esperaba 'public' al inicio de la declaración del método");
        }
    }

//------------------------------------------------------------------------------
    private void encab_metodo() {
        if (preAnalisis.equals("public")) {
            //encab_metodo → public static tipo_metodo id ( lista_parametros )
            emparejar("public");
            emparejar("static");
            tipo_metodo();
            emparejar("id");
            emparejar("(");
            lista_parametros();
            emparejar(")");
        } else {
            error("[encab_metodo] Error: Se esperaba 'public' al inicio del encabezado del método");
        }
    }

//------------------------------------------------------------------------------
    private void metodo_principal() {
        if (preAnalisis.equals("public")) {
            //metodo_principal → public static void main ( string args [ ] ) proposición_compuesta
            emparejar("public");
            emparejar("static");
            emparejar("void");
            emparejar("main");
            emparejar("(");
            emparejar("string");
            emparejar("args");
            emparejar("[");
            emparejar("]");
            emparejar(")");
            proposicion_compuesta();
        } else {
            error("[metodo_principal] Error: Se esperaba 'public' al inicio del método principal");
        }
    }

//------------------------------------------------------------------------------
    private void tipo_metodo() {
        if (preAnalisis.equals("void")) {
            //tipo_metodo → void | tipo_estandar corchetes
            emparejar("void");
        } else if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            tipo_estandar();
            corchetes();
        } else {
            error("[tipo_metodo] Error: Se esperaba 'void' o un tipo estándar");
        }
    }

//------------------------------------------------------------------------------
    private void corchetes() {
        if (preAnalisis.equals("[")) {
            //corchetes → [ ] | ϵ
            emparejar("[");
            emparejar("]");
        }
        // Caso ϵ: No se hace nada.
    }

//------------------------------------------------------------------------------
    private void lista_parametros() {
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            //lista_parametros → tipo id dimension lista_parametros’ | ϵ
            tipo();
            emparejar("id");
            dimension();
            lista_parametros_prima();
        }
        // Caso ϵ: No se hace nada.
    }

//------------------------------------------------------------------------------
    private void lista_parametros_prima() {
        if (preAnalisis.equals(",")) {
            //lista_parametros' → , tipo id dimension lista_parametros' | ϵ
            emparejar(",");
            tipo();
            emparejar("id");
            dimension();
            lista_parametros_prima();
        }
        // Caso ϵ: No se hace nada.
    }

//------------------------------------------------------------------------------    
    private void proposicion_compuesta() {
        if (preAnalisis.equals("{")) {
            //proposición_compuesta → { proposiciones_optativas }
            emparejar("{");
            proposiciones_optativas();
            emparejar("}");
        } else {
            error("[proposición_compuesta] Error: Se esperaba '{'");
        }
    }

//------------------------------------------------------------------------------
    private void proposiciones_optativas() {
        if (preAnalisis.equals("id") || preAnalisis.equals("if") || preAnalisis.equals("while")) {
            //proposiciones_optativas → lista_proposiciones | ϵ
            lista_proposiciones();
        }
        // Caso ϵ: no se hace nada si no hay proposiciones
    }

//------------------------------------------------------------------------------
    private void lista_proposiciones() {
        if (preAnalisis.equals("id") || preAnalisis.equals("if") || preAnalisis.equals("while")) {
            //lista_proposiciones → proposición lista_proposiciones | ϵ
            proposicion();
            lista_proposiciones();
        } else {
            // Caso ϵ: no se hace nada si no hay proposiciones
        }

    }

//------------------------------------------------------------------------------
    private void proposicion() {
        if (preAnalisis.equals("id")) {
            //proposición → id proposición' ; | proposición_compuesta | if ( expresión ) proposición else proposición | while ( expresión ) proposición
            emparejar("id");
            proposicion_prima();
            emparejar(";");
        } else if (preAnalisis.equals("if")) {
            emparejar("if");
            emparejar("(");
            expresion();
            emparejar(")");
            proposicion();
            if (preAnalisis.equals("else")) {
                emparejar("else");
                proposicion();
            }
        } else if (preAnalisis.equals("while")) {
            emparejar("while");
            emparejar("(");
            expresion();
            emparejar(")");
            proposicion();
        } else if (preAnalisis.equals("{")) {
            proposicion_compuesta();
        } else {
            error("[proposición] Error: Se esperaba 'id', 'if', 'while' o '{'");
        }
    }

//------------------------------------------------------------------------------
    private void proposicion_prima() {
        if (preAnalisis.equals("[")) {
            //proposición' → [ expresión ] opasig expresión | opasig expresión | proposición_metodo | ϵ
            emparejar("[");
            expresion();
            emparejar("]");
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("opasig")) {
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("(")) {
            proposicion_metodo();
        }
        // Caso ϵ: no se hace nada si no hay nada más.
    }
//------------------------------------------------------------------------------

    private void proposicion_metodo() {
        if (preAnalisis.equals("(")) {
            //proposición_metodo → ( lista_expresiones ) | ϵ
            emparejar("(");
            lista_expresiones();
            emparejar(")");
        } else {
            // Caso ϵ: no se hace nada si no hay un paréntesis

        }
    }

//------------------------------------------------------------------------------
    private void lista_expresiones() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {
            //lista_expresiones → expresión lista_expresiones' | ϵ
            expresion();
            lista_expresiones_prima();
        }
        // Caso ϵ: no se hace nada si no pertenece a los primeros de 'expresión'
    }
//------------------------------------------------------------------------------

    private void lista_expresiones_prima() {
        if (preAnalisis.equals(",")) {
            emparejar(",");                 
            expresion();                     
            lista_expresiones_prima();       
        }
        // Caso ϵ: no se hace nada si no hay coma
    }

//------------------------------------------------------------------------------
//--------------------------------------------------------------------------    
private void expresion() {
    if (preAnalisis.equals("literal")) {
        // expresión → literal
        emparejar("literal");
    } else if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {
        // expresión → expresión_simple expresión'
        expresion_simple();
        expresion_prima();
    } else {
        error("[expresión] Error: Se esperaba un 'literal' o una 'expresión_simple' en la línea " + cmp.be.preAnalisis.numLinea);
    }
}

//--------------------------------------------------------------------------    
private void expresion_prima() {
    if (preAnalisis.equals("oprel")) {
        // expresión' → oprel expresión_simple
        emparejar("oprel");
        expresion_simple();
    }
    // Caso ϵ: no se hace nada si no hay más elementos.
}

//--------------------------------------------------------------------------    
private void expresion_simple() {
    // expresión_simple → termino expresión_simple'
    termino();
    expresion_simple_prima();
}

//--------------------------------------------------------------------------    
private void expresion_simple_prima() {
    if (preAnalisis.equals("opsuma")) {
        // expresión_simple' → opsuma termino expresión_simple'
        emparejar("opsuma");
        termino();
        expresion_simple_prima();
    }
    // Caso ϵ: no se hace nada si no hay más operadores.
}

//--------------------------------------------------------------------------    
private void termino() {
    // termino → factor termino'
    factor();
    termino_prima();
}

//--------------------------------------------------------------------------    
private void termino_prima() {
    if (preAnalisis.equals("opmult")) {
        // termino' → opmult factor termino'
        emparejar("opmult");
        factor();
        termino_prima();
    }
    // Caso ϵ: no se hace nada si no hay más operadores.
}

//--------------------------------------------------------------------------    
private void factor() {
    if (preAnalisis.equals("id")) {
        // factor → id factor'
        emparejar("id");
        factor_prima();
    } else if (preAnalisis.equals("num")) {
        // factor → num
        emparejar("num");
    } else if (preAnalisis.equals("num.num")) {
        // factor → num.num
        emparejar("num.num");
    } else if (preAnalisis.equals("(")) {
        // factor → ( expresión )
        emparejar("(");
        expresion();
        emparejar(")");
    } else {
        error("[factor] Error: Se esperaba un 'id', 'num', 'num.num' o '(' en la línea " + cmp.be.preAnalisis.numLinea);
    }
}

//--------------------------------------------------------------------------    
private void factor_prima() {
    if (preAnalisis.equals("(")) {
        // factor' → ( lista_expresiones )
        emparejar("(");
        lista_expresiones();
        emparejar(")");
    }
    // Caso ϵ: no se hace nada si no hay un paréntesis.
}

//------------------------------------------------------------------------------    
} //FIN SintacticoSemantico
//------------------------------------------------------------------------------
//::
