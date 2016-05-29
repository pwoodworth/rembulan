package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.util.Check;

public class LibUtils {

	public static final String MT_NAME = "__name";

	public static Table init(LuaState state, Lib lib) {
		Check.notNull(state);
		Table env = state.newTable(0, 0);
		lib.installInto(state, env);
		return env;
	}

	public static void setIfNonNull(Table table, String key, Object value) {
		Check.notNull(table);
		if (value != null) {
			table.rawset(key, value);
		}
	}

	public static Object checkValue(String name, Object[] args, int index) {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new BadArgumentException((index + 1), name, "value expected");
		}
	}

	public interface ValueTypeNamer {

		String typeNameOf(Object instance);

	}

	public static class PlainValueTypeNamer implements ValueTypeNamer {

		public static final PlainValueTypeNamer INSTANCE = new PlainValueTypeNamer();

		@Override
		public String typeNameOf(Object instance) {
			return Value.typeOf(instance).name;
		}

	}

	public static class NameMetamethodValueTypeNamer implements ValueTypeNamer {

		private final LuaState state;

		public NameMetamethodValueTypeNamer(LuaState state) {
			this.state = Check.notNull(state);
		}

		@Override
		public String typeNameOf(Object instance) {
			Table mt = Metatables.getMetatable(state, instance);
			if (mt != null) {
				Object nameField = mt.rawget(MT_NAME);
				if (nameField instanceof String) {
					return (String) nameField;
				}
			}
			return Value.typeOf(instance).name;
		}

	}

	// FIXME: clean this up: redundant code!
	public static Number checkNumber(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				return n;
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static int checkInt(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				Long l = Conversions.numberAsLong(n);
				if (l != null) {
					long ll = l;
					if (ll >= Integer.MIN_VALUE && ll <= Integer.MAX_VALUE) {
						return (int) ll;
					}
				}

				throw new IllegalArgumentException("number has no integer representation");
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static long checkInteger(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				Long l = Conversions.numberAsLong(n);
				if (l != null) {
					return l;
				}
				else {
					throw new IllegalArgumentException("number has no integer representation");
				}
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static int checkRange(ValueTypeNamer namer, String name, Object[] args, int index, String rangeName, int min, int max) {
		final String what;

		if (index < args.length) {
			Object o = args[index];
			Number n = Conversions.objectAsNumber(o);

			if (n != null) {
				Integer i = Conversions.numberAsInt(n);
				if (i != null) {
					int ii = i;
					if (ii >= min && ii <= max) {
						return ii;
					}
					else {
						throw new BadArgumentException((index + 1), name, rangeName + " out of range");
					}
				}
				else {
					throw new BadArgumentException((index + 1), name, "number has no integer representation");
				}
			}
			else {
				what = namer.typeNameOf(o);
			}
		}
		else {
			what = "no value";
		}

		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static String checkString(ValueTypeNamer namer, String name, Object[] args, int index, boolean strict) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (strict) {
				if (arg instanceof String) {
					return (String) arg;
				}
			}
			else {
				String s = Conversions.objectAsString(arg);
				if (s != null) {
					return s;
				}
			}

			// not a string!
			what = namer.typeNameOf(arg);
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "string expected, got " + what);
	}

	public static String checkString(ValueTypeNamer namer, String name, Object[] args, int index) {
		return checkString(namer, name, args, index, false);
	}

	public static Table checkTableOrNil(String name, Object[] args, int index) {
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Table) {
				return (Table) arg;
			}
			else if (arg == null) {
				return null;
			}
		}
		throw new BadArgumentException((index + 1), name, "nil or table expected");
	}

	// FIXME: clean this up: redundant code!
	public static Table checkTable(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Table) {
				return (Table) arg;
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "table expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static Function checkFunction(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Function) {
				return (Function) arg;
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "function expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static Coroutine checkCoroutine(ValueTypeNamer namer, String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Coroutine) {
				return (Coroutine) arg;
			}
			else {
				what = namer.typeNameOf(arg);
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "coroutine expected, got " + what);
	}

}
