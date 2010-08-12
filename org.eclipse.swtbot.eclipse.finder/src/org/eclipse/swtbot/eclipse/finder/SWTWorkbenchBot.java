/*******************************************************************************
 * Copyright (c) 2009,2010 SWTBot Committers and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ralf Ebert www.ralfebert.de - (bug 271630) SWTBot Improved RCP / Workbench support
 *     Ketan Padegaonkar - (bug 260088) Support for MultiPageEditorPart
 *******************************************************************************/
package org.eclipse.swtbot.eclipse.finder;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartId;
import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPerspectiveId;
import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPerspectiveLabel;
import static org.eclipse.swtbot.eclipse.finder.waits.Conditions.waitForEditor;
import static org.eclipse.swtbot.eclipse.finder.waits.Conditions.waitForView;
import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.finders.WorkbenchContentsFinder;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.waits.WaitForEditor;
import org.eclipse.swtbot.eclipse.finder.waits.WaitForView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * SWTWorkbenchBot is a {@link SWTBot} with capabilities for testing Eclipse workbench items like views, editors and
 * perspectives
 * 
 * @author Ralf Ebert www.ralfebert.de (bug 271630)
 * @version $Id$
 */
public class SWTWorkbenchBot extends SWTBot {

	private final WorkbenchContentsFinder	workbenchContentsFinder;

	/**
	 * Constructs a workbench bot
	 */
	public SWTWorkbenchBot() {
		workbenchContentsFinder = new WorkbenchContentsFinder();
	}

	/**
	 * Returns the perspective matching the given matcher
	 * 
	 * @param matcher the matcher used to find the perspective
	 * @return a perspective matching the matcher
	 * @throws WidgetNotFoundException if the perspective is not found
	 */
	public SWTBotPerspective perspective(Matcher<?> matcher) {
		List<IPerspectiveDescriptor> perspectives = workbenchContentsFinder.findPerspectives(matcher);
		return new SWTBotPerspective(perspectives.get(0), this);
	}

	/**
	 * Shortcut for perspective(withPerspectiveLabel(label))
	 * 
	 * @param label the "human readable" label for the perspective
	 * @return a perspective with the specified <code>label</code>
	 * @see #perspective(Matcher)
	 * @see WidgetMatcherFactory#withPerspectiveLabel(Matcher)
	 */
	public SWTBotPerspective perspectiveByLabel(String label) {
		return perspective(withPerspectiveLabel(label));
	}

	/**
	 * Shortcut for perspective(perspectiveById(label))
	 * 
	 * @param id the perspective id
	 * @return a perspective with the specified <code>label</code>
	 * @see #perspective(Matcher)
	 * @see WidgetMatcherFactory#withPerspectiveId(Matcher)
	 */
	public SWTBotPerspective perspectiveById(String id) {
		return perspective(withPerspectiveId(id));
	}

	/**
	 * @param matcher Matcher for IPerspectiveDescriptor
	 * @return all available matching perspectives
	 */
	public List<SWTBotPerspective> perspectives(Matcher<?> matcher) {
		List<IPerspectiveDescriptor> perspectives = workbenchContentsFinder.findPerspectives(matcher);

		List<SWTBotPerspective> perspectiveBots = new ArrayList<SWTBotPerspective>();
		for (IPerspectiveDescriptor perspectiveDescriptor : perspectives)
			perspectiveBots.add(new SWTBotPerspective(perspectiveDescriptor, this));

		return perspectiveBots;
	}

	/**
	 * @return all available perspectives
	 */
	public List<SWTBotPerspective> perspectives() {
		return perspectives(Matchers.anything());
	}

	/**
	 * Waits for a view matching the given matcher to appear in the active workbench page and returns it
	 * 
	 * @param matcher the matcher used to match views
	 * @return views that match the matcher
	 * @throws WidgetNotFoundException if the view is not found
	 */
	public SWTBotView view(Matcher<IViewReference> matcher) {
		WaitForView waitForView = waitForView(matcher);
		waitUntilWidgetAppears(waitForView);
		return new SWTBotView(waitForView.get(0), this);
	}

	/**
	 * Shortcut for view(withPartName(title))
	 * 
	 * @param title the "human readable" title
	 * @return the view with the specified title
	 * @see WidgetMatcherFactory#withPartName(Matcher)
	 */
	public SWTBotView viewByTitle(String title) {
		Matcher<IViewReference> withPartName = withPartName(title);
		return view(withPartName);
	}

	/**
	 * Shortcut for view(withPartId(id))
	 * 
	 * @param id the view id
	 * @return the view with the specified id
	 * @see WidgetMatcherFactory#withPartId(String)
	 */
	public SWTBotView viewById(String id) {
		Matcher<IViewReference> withPartId = withPartId(id);
		return view(withPartId);
	}

	/**
	 * Returns all views which are opened currently (no waiting!) which match the given matcher
	 * 
	 * @param matcher the matcher used to find views
	 * @return the list of all matching views
	 */
	public List<SWTBotView> views(Matcher<?> matcher) {
		List<IViewReference> views = workbenchContentsFinder.findViews(matcher);

		List<SWTBotView> viewBots = new ArrayList<SWTBotView>();
		for (IViewReference viewReference : views)
			viewBots.add(new SWTBotView(viewReference, this));
		return viewBots;
	}

	/**
	 * @return all views which are opened currently
	 */
	public List<SWTBotView> views() {
		return views(Matchers.anything());
	}

	/**
	 * Returns the active workbench view part
	 * 
	 * @return the active view, if any
	 * @throws WidgetNotFoundException if there is no active view
	 */
	public SWTBotView activeView() {
		IViewReference view = workbenchContentsFinder.findActiveView();
		if (view == null)
			throw new WidgetNotFoundException("There is no active view"); //$NON-NLS-1$
		return new SWTBotView(view, this);
	}

	/**
	 * Waits for a editor matching the given matcher to appear in the active workbench page and returns it
	 * 
	 * @param matcher the matcher used to find the editor
	 * @return an editor that matches the matcher
	 * @throws WidgetNotFoundException if the editor is not found
	 */
	public SWTBotEditor editor(Matcher<IEditorReference> matcher) {
		WaitForEditor waitForEditor = waitForEditor(matcher);
		waitUntilWidgetAppears(waitForEditor);
		return new SWTBotEditor(waitForEditor.get(0), this);
	}

	/**
	 * @return all editors which are opened currently (no waiting!) which match the given matcher
	 * @param matcher the matcher used to find all editors
	 */
	public List<SWTBotEditor> editors(Matcher<?> matcher) {
		List<IEditorReference> editors = workbenchContentsFinder.findEditors(matcher);

		List<SWTBotEditor> editorBots = new ArrayList<SWTBotEditor>();
		for (IEditorReference editorReference : editors)
			editorBots.add(new SWTBotEditor(editorReference, this));

		return editorBots;
	}

	/**
	 * @return all editors which are opened currently
	 */
	public List<? extends SWTBotEditor> editors() {
		return editors(Matchers.anything());
	}

	/**
	 * Shortcut for editor(withPartName(title))
	 * 
	 * @param fileName the the filename on the editor tab
	 * @return the editor with the specified title
	 * @see #editor(Matcher)
	 */
	public SWTBotEditor editorByTitle(String fileName) {
		Matcher<IEditorReference> withPartName = withPartName(fileName);
		return editor(withPartName);
	}

	/**
	 * Shortcut for editor(withPartId(id))
	 * 
	 * @param id the the id on the editor tab
	 * @return the editor with the specified title
	 * @see #editor(Matcher)
	 */
	public SWTBotEditor editorById(String id) {
		Matcher<IEditorReference> withPartId = withPartId(id);
		return editor(withPartId);
	}

	/**
	 * Returns the active workbench editor part
	 * 
	 * @return the active editor, if any
	 * @throws WidgetNotFoundException if there is no active view
	 */
	public SWTBotEditor activeEditor() {
		IEditorReference editor = workbenchContentsFinder.findActiveEditor();
		if (editor == null)
			throw new WidgetNotFoundException("There is no active editor"); //$NON-NLS-1$
		return new SWTBotEditor(editor, this);
	}
	
	/**
	 * Waits for a multipage editor matching the given matcher to appear in the active workbench page and returns it
	 * 
	 * @param matcher the matcher used to find the editor
	 * @return an editor that matches the matcher
	 * @throws WidgetNotFoundException if the editor is not found
	 */
	public SWTBotMultiPageEditor multipageEditor(Matcher<IEditorReference> matcher) {
		WaitForEditor waitForEditor = waitForEditor(matcher);
		waitUntilWidgetAppears(waitForEditor);
		return new SWTBotMultiPageEditor(waitForEditor.get(0), this);
	}

	/**
	 * @return all multipage editors which are opened currently (no waiting!) which match the given matcher
	 * @param matcher the matcher used to find all editors
	 */
	public List<SWTBotMultiPageEditor> multipageEditors(Matcher<?> matcher) {
		List<IEditorReference> editors = workbenchContentsFinder.findEditors(matcher);

		List<SWTBotMultiPageEditor> editorBots = new ArrayList<SWTBotMultiPageEditor>();
		for (IEditorReference editorReference : editors)
			editorBots.add(new SWTBotMultiPageEditor(editorReference, this));

		return editorBots;
	}

	/**
	 * @return all editors which are opened currently
	 */
	public List<? extends SWTBotMultiPageEditor> multipageEditors() {
		return multipageEditors(Matchers.anything());
	}

	/**
	 * Shortcut for multipageEditor(withPartName(title))
	 * 
	 * @param fileName the the filename on the editor tab
	 * @return the editor with the specified title
	 * @see #editor(Matcher)
	 */
	public SWTBotMultiPageEditor multipageEditorByTitle(String fileName) {
		Matcher<IEditorReference> withPartName = withPartName(fileName);
		return multipageEditor(withPartName);
	}

	/**
	 * Shortcut for multipageEditor(withPartId(id))
	 * 
	 * @param id the the id on the editor tab
	 * @return the editor with the specified title
	 * @see #editor(Matcher)
	 */
	public SWTBotMultiPageEditor multipageEditorById(String id) {
		Matcher<IEditorReference> withPartId = withPartId(id);
		return multipageEditor(withPartId);
	}
	
	/**
	 * @return the active perspective in the active workbench page
	 */
	public SWTBotPerspective activePerspective() {
		IPerspectiveDescriptor perspective = workbenchContentsFinder.findActivePerspective();
		if (perspective == null)
			throw new WidgetNotFoundException("There is no active perspective"); //$NON-NLS-1$
		return new SWTBotPerspective(perspective, this);
	}

	/**
	 * Does a <em>best effort</em> to reset the workbench. This method attempts to:
	 * <ul>
	 * <li>close all non-workbench windows</li>
	 * <li>save and close all open editors</li>
	 * <li>reset the <em>active</em> perspective</li>
	 * <li>switch to the default perspective for the workbench</li>
	 * <li>reset the <em>default</em> perspective for the workbench</li>
	 * <ul>
	 */
	public void resetWorkbench() {
		closeAllShells();
		saveAllEditors();
		closeAllEditors();
		resetActivePerspective();

		defaultPerspective().activate();
		resetActivePerspective();
	}

	/**
	 * Returns the default perspective as defined in the WorkbenchAdvisor of the application.
	 */
	public SWTBotPerspective defaultPerspective() {
		return syncExec(new Result<SWTBotPerspective>() {

			public SWTBotPerspective run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				return perspectiveById(workbench.getPerspectiveRegistry().getDefaultPerspective());
			}
		});
	}

	public void closeAllEditors() {
		new DefaultWorkbench(this).closeAllEditors();
	}

	public void saveAllEditors() {
		new DefaultWorkbench(this).saveAllEditors();
	}

	public SWTBotPerspective resetActivePerspective() {
		new DefaultWorkbench(this).resetActivePerspective();
		return activePerspective();
	}

	public void closeAllShells() {
		new DefaultWorkbench(this).closeAllShells();
	}

}
