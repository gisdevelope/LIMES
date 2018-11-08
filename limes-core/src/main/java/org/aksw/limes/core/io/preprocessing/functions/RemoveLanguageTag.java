package org.aksw.limes.core.io.preprocessing.functions;

import java.util.TreeSet;

import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.preprocessing.APreprocessingFunction;
import org.aksw.limes.core.io.preprocessing.IPreprocessingFunction;

/**
 * Deletes everything after and including <code>@</code>
 * Used to delete language tags so e.g. <code>Ibuprofen@en</code> will become <code>Ibuprofen</code> 
 * @author Daniel Obraczka
 *
 */
public class RemoveLanguageTag extends APreprocessingFunction implements IPreprocessingFunction {

	@Override
	public Instance applyFunctionAfterCheck(Instance i, String property, String... arguments) {
            TreeSet<String> oldValues = i.getProperty(property);
            TreeSet<String> newValues = new TreeSet<>();
            for (String value : oldValues) {
            	if(value.contains(AT)){
            		newValues.add(value.substring(0, value.lastIndexOf(AT)));
            	}else{
            		newValues.add(value);
            	}
            }
            i.replaceProperty(property, newValues);
		return i;
	}

	@Override
	public int minNumberOfArguments() {
		return 0;
	}

	@Override
	public int maxNumberOfArguments() {
		return 0;
	}

}
