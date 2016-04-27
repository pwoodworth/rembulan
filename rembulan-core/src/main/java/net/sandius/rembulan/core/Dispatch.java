package net.sandius.rembulan.core;

public abstract class Dispatch {

	private Dispatch() {
		// not to be instantiated or extended
	}

	public static Invokable callTarget(LuaState state, Object target) {
		if (target instanceof Invokable) {
			return (Invokable) target;
		}
		else {
			Object handler = Metatables.getMetamethod(state, Metatables.MT_CALL, target);

			if (handler instanceof Invokable) {
				return (Invokable) handler;
			}
			else {
				throw IllegalOperationAttemptException.call(target);
			}
		}
	}

	public static void mt_invoke(ExecutionContext context, Object target) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context);
		else fn.invoke(context, target);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1);
		else fn.invoke(context, target, arg1);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2);
		else fn.invoke(context, target, arg1, arg2);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3);
		else fn.invoke(context, target, arg1, arg2, arg3);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4);
		else fn.invoke(context, target, arg1, arg2, arg3, arg4);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4, arg5);
		else fn.invoke(context, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object[] args) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) {
			fn.invoke(context, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			fn.invoke(context, mtArgs);
		}
	}

	public static void evaluateTailCalls(ExecutionContext context) throws ControlThrowable {
		ObjectSink r = context.getObjectSink();
		while (r.isTailCall()) {
			Object target = r.getTailCallTarget();
			switch (r.size()) {
				case 0: mt_invoke(context, target); break;
				case 1: mt_invoke(context, target, r._0()); break;
				case 2: mt_invoke(context, target, r._0(), r._1()); break;
				case 3: mt_invoke(context, target, r._0(), r._1(), r._2()); break;
				case 4: mt_invoke(context, target, r._0(), r._1(), r._2(), r._3()); break;
				case 5: mt_invoke(context, target, r._0(), r._1(), r._2(), r._3(), r._4()); break;
				default: mt_invoke(context, target, r.toArray()); break;
			}
		}
	}

	public static void call(ExecutionContext context, Object target) throws ControlThrowable {
		mt_invoke(context, target);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1) throws ControlThrowable {
		mt_invoke(context, target, arg1);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3, arg4);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3, arg4, arg5);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object[] args) throws ControlThrowable {
		mt_invoke(context, target, args);
		evaluateTailCalls(context);
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(a, b);
		}
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(o);
		}
	}

	public static void add(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation math = MathImplementation.arithmetic(a, b);
		if (math != null) {
			context.getObjectSink().setTo(math.do_add(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_ADD, a, b);
		}
	}

	public static Number add(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_add(a, b);
	}

	public static void sub(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_sub(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_SUB, a, b);
		}
	}

	public static Number sub(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_sub(a, b);
	}

	public static void mul(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_mul(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MUL, a, b);
		}
	}

	public static Number mul(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_mul(a, b);
	}

	public static void div(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_div(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_DIV, a, b);
		}
	}

	public static Number div(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_div(a, b);
	}

	public static void mod(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_mod(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MOD, a, b);
		}
	}

	public static Number mod(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_mod(a, b);
	}

	public static void idiv(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_idiv(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_IDIV, a, b);
		}
	}

	public static Number idiv(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_idiv(a, b);
	}

	public static void pow(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			context.getObjectSink().setTo(m.do_pow(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_POW, a, b);
		}
	}

	public static Number pow(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_pow(a, b);
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(a, b);
		}
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(o);
		}
	}

	public static void band(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			context.getObjectSink().setTo(la & lb);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BAND, a, b);
		}
	}

	public static void bor(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			context.getObjectSink().setTo(la | lb);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BOR, a, b);
		}
	}

	public static void bxor(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			context.getObjectSink().setTo(la ^ lb);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BXOR, a, b);
		}
	}

	public static void shl(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			context.getObjectSink().setTo(la << lb);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHL, a, b);
		}
	}

	public static void shr(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			context.getObjectSink().setTo(la >>> lb);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHR, a, b);
		}
	}

	public static void unm(ExecutionContext context, Object o) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(o);

		if (m != null) {
			context.getObjectSink().setTo(m.do_unm(Conversions.objectAsNumber(o)));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_UNM, o);
		}
	}

	public static Number unm(Number n) {
		return MathImplementation.arithmetic(n).do_unm(n);
	}

	public static void bnot(ExecutionContext context, Object o) throws ControlThrowable {
		Long lo = Conversions.objectAsLong(o);

		if (lo != null) {
			context.getObjectSink().setTo(~lo);
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BNOT, o);
		}
	}

	public static void len(ExecutionContext context, Object o) throws ControlThrowable {
		if (o instanceof String) {
			context.getObjectSink().setTo((long) RawOperators.stringLen((String) o));
		}
		else {
			Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_LEN, o);
			if (handler != null) {
				call(context, handler, o);
			}
			else if (o instanceof Table) {
				context.getObjectSink().setTo((long) ((Table) o).rawlen());
			}
			else {
				throw IllegalOperationAttemptException.length(o);
			}
		}
	}

	public static void concat(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		String sa = Conversions.objectAsString(a);
		String sb = Conversions.objectAsString(b);

		if (sa != null && sb != null) {
			context.getObjectSink().setTo(sa.concat(sb));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_CONCAT, a, b);
			if (handler != null) {
				call(context, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.concatenate(a, b);
			}
		}
	}

	private static class ComparisonResumable implements Resumable {

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Boolean b = (Boolean) suspendedState;
			ObjectSink result = context.getObjectSink();
			boolean resultValue = Conversions.objectToBoolean(result._0());
			result.setTo(b == resultValue);
		}

	}

	private static void _call_comparison_mt(ExecutionContext context, boolean cmpTo, Object handler, Object a, Object b) throws ControlThrowable {
		try {
			call(context, handler, a, b);
		}
		catch (ControlThrowable ct) {
			// suspended in the metamethod call
			ct.push(new ComparisonResumable(), cmpTo);
			throw ct;
		}
		// not suspended: set the result, possibly flipping it
		ObjectSink result = context.getObjectSink();
		result.setTo(Conversions.objectToBoolean(result._0()) == cmpTo);
	}

	public static void eq(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		boolean rawEqual = RawOperators.raweq(a, b);

		if (!rawEqual
				&& ((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))) {

			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_EQ, a, b);

			if (handler != null) {
				_call_comparison_mt(context, true, handler, a, b);
				return;
			}

			// else keep the result as false
		}

		context.getObjectSink().setTo(rawEqual);
	}

	public static boolean eq(Number a, Number b) {
		return ComparisonImplementation.of(a, b).do_eq(a, b);
	}


	public static void lt(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			context.getObjectSink().setTo(c.do_lt(a, b));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_LT, a, b);

			if (handler != null) {
				_call_comparison_mt(context, true, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.comparison(a, b);
			}
		}
	}

	public static boolean lt(Number a, Number b) {
		return ComparisonImplementation.of(a, b).do_lt(a, b);
	}

	public static void le(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			context.getObjectSink().setTo(c.do_le(a, b));
		}
		else {
			LuaState state = context.getState();
			Object le_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LE, a, b);

			if (le_handler != null) {
				_call_comparison_mt(context, true, le_handler, a, b);
			}
			else {
				// TODO: verify that (a, b) is the order in which the metamethod is looked up
				Object lt_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);

				if (lt_handler != null) {
					// will be evaluating "not (b < a)"
					_call_comparison_mt(context, false, lt_handler, b, a);
				}
				else {
					throw IllegalOperationAttemptException.comparison(a, b);
				}
			}
		}
	}

	public static boolean le(Number a, Number b) {
		return ComparisonImplementation.of(a, b).do_le(a, b);
	}

	public static void index(ExecutionContext context, Object table, Object key) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object value = t.rawget(key);

			if (value != null) {
				context.getObjectSink().setTo(value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			// key not found and no index metamethod, returning nil
			context.getObjectSink().setTo(null);
			return;
		}
		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(context, handler, table, key);
			evaluateTailCalls(context);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			index(context, handler, key);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	public static void newindex(ExecutionContext context, Object table, Object key, Object value) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object r = t.rawget(key);

			if (r != null) {
				t.rawset(key, value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_NEWINDEX, table);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return;
		}

		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(context, handler, table, key, value);
			evaluateTailCalls(context);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			newindex(context, handler, key, value);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	private static boolean isNonZero(MathImplementation m, Number n) {
		return !m.do_eq(0L, n);
	}

	public static boolean continueLoop(Number index, Number limit, Number step) {
		MathImplementation m_step = MathImplementation.arithmetic(step, 0L);
		if (!isNonZero(m_step, step)) {
			return false;  // step is zero or NaN
		}

		boolean ascending = m_step.do_lt(0L, step);

		MathImplementation m_cmp = MathImplementation.arithmetic(index, limit);
		return ascending
				? m_cmp.do_le(index, limit)
				: m_cmp.do_le(limit, index);
	}

}
