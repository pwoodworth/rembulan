/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.load.ChunkLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.util.Objects;

/**
 * The configuration of the Lua standard library.
 *
 * <p>This is an immutable class that provides transformation methods for manipulating
 * the configuration, and the {@link #installInto(StateContext)} method for installing
 * the standard library with the specified configuration into a Lua state.</p>
 */
public class StdLibConfig {

	private final RuntimeEnvironment environment;

	private final ChunkLoader loader;
	private final boolean withDebug;

	private StdLibConfig(RuntimeEnvironment environment,
			ChunkLoader loader, boolean withDebug) {

		this.environment = Objects.requireNonNull(environment);
		this.loader = loader;
		this.withDebug = withDebug;
	}

	private StdLibConfig(RuntimeEnvironment environment) {
		this(environment, null, false);
	}

	/**
	 * Returns a default configuration for the specified environment.
	 * The default configuration does not include the Debug library and has no chunk loader.
	 *
	 * <p>If any of the standard streams defined by the runtime environment is {@code null},
	 * the corresponding file in the I/O library (such as {@code io.stdin}) will be undefined.
	 * Additionally, if {@code out} is {@code null}, then the global function {@code print}
	 * will be undefined.</p>
	 *
	 * @param environment  the runtime environment, must not be {@code null}
	 * @return  the default configuration
	 *
	 * @throws NullPointerException  if {@code environment} is {@code null}
	 */
	public static StdLibConfig of(RuntimeEnvironment environment) {
		return new StdLibConfig(environment);
	}

	/**
	 * Returns a configuration that differs from this configuration in that
	 * it uses the chunk loader {@code loader}. If {@code loader} is {@code null}, no
	 * chunk loader is used.
	 *
	 * @param loader  the chunk loader, may be {@code null}
	 * @return  a configuration that uses {@code loader} as its chunk loader
	 */
	public StdLibConfig withLoader(ChunkLoader loader) {
		return new StdLibConfig(environment, loader, withDebug);
	}

	/**
	 * Returns a configuration that includes the Debug library iff {@code withDebug}
	 * is {@code true}.
	 *
	 * @param withDebug  boolean flag indicating whether to include the Debug library
	 * @return  a configuration that includes the Debug library iff {@code withDebug} is
	 *          {@code true}
	 */
	public StdLibConfig setDebug(boolean withDebug) {
		return this.withDebug != withDebug
				? new StdLibConfig(environment, loader, withDebug)
				: this;
	}

	/**
	 * Installs the standard library into {@code state}, returning a new table suitable
	 * for use as the global upvalue.
	 *
	 * @param state  the Lua state context to install into, must not be {@code null}
	 * @return  a new table containing the standard library
	 *
	 * @throws NullPointerException  if {@code state is null}
	 * @throws IllegalStateException  if the configuration is invalid
	 */
	public Table installInto(StateContext state) {
		Objects.requireNonNull(state);
		Table env = state.newTable();

		InputStream in = environment.standardInput();
		OutputStream out = environment.standardOutput();
		OutputStream err = environment.standardError();
		FileSystem fileSystem = environment.fileSystem();

		new DefaultBasicLib(out != null ? new PrintStream(out) : null, loader, env).installInto(state, env);
		ModuleLib moduleLib = new DefaultModuleLib(state, env);
		moduleLib.installInto(state, env);
		moduleLib.install(new DefaultCoroutineLib());
		moduleLib.install(new DefaultStringLib());
		moduleLib.install(new DefaultMathLib());
		moduleLib.install(new DefaultTableLib());
		moduleLib.install(new DefaultIoLib(state, fileSystem, in, out, err));
		moduleLib.install(new DefaultOsLib(environment));
		moduleLib.install(new DefaultUtf8Lib());
		moduleLib.install(new DefaultDebugLib());
		return env;
	}

}
