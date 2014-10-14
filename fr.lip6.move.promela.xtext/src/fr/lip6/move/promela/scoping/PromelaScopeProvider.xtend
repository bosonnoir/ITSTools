/*
 * generated by Xtext
 */
package fr.lip6.move.promela.scoping

import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

/**
 * This class contains custom Promela scoping description.
 * 
 * see : http://www.eclipse.org/Xtext/documentation.html#scoping
 * on how and when to use it 
 *
 */
class PromelaScopeProvider extends AbstractDeclarativeScopeProvider {
// FIXME: adapt scope!
// DISABLED
//	override IScope getScope(EObject context, EReference reference) {
//
//		// Pour l'instant juste une délégation avec tracage
//		println(
//			'''scope  «reference.EContainingClass.name»
//			 in «reference.name» contexte «context.eClass.name»  ''')
//
//		return super.getScope(context, reference)
// TODO!: référence à macro cassée avec la ref
// }


	/*
		 * En gros switch:
		 * - si Assignemenbt: juste les variables dans la portée considérée
		 * - sinon tout: variable, à portée + macro
		 * 
		 * 
		 * Besoin méthode pour collecter: 
		 * - les variabes locales (imbrication des blocs
		 *    TODO: voir scoping, shadowing, etc
		 * - les paramètres du proc
		 * - variables globales directement accessibles depuis la promspecification
		 * 
		 * si fait ceci, aurait qu'à récupérer valeur dans container correspondant.
		 * (après analyse statique. HOWTO?)
		 * 
		 * 
		 * 
		 */


}
