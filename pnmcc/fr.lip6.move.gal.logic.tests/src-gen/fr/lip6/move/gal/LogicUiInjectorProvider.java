/*
* generated by Xtext
*/
package fr.lip6.move.gal;

import org.eclipse.xtext.junit4.IInjectorProvider;

import com.google.inject.Injector;

public class LogicUiInjectorProvider implements IInjectorProvider {
	
	public Injector getInjector() {
		return fr.lip6.move.gal.ui.internal.LogicActivator.getInstance().getInjector("fr.lip6.move.gal.Logic");
	}
	
}