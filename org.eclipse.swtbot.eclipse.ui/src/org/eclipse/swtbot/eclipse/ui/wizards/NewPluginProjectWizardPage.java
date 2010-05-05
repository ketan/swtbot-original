/*******************************************************************************
 * Copyright (c) 2010 Ketan Padegaonkar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ketan Padegaonkar - initial API and implementation
 *******************************************************************************/
package org.eclipse.swtbot.eclipse.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewPluginProjectWizardPage extends WizardPage implements WizardPageSettings {

	private Button	productIdButton;
	private Button	applicationIdButton;
	private Combo	productId;
	private Combo	applicationId;

	private Text	pluginId;
	private Text	pluginName;
	private Text	pluginVersion;
	private Text	pluginProvider;

	protected NewPluginProjectWizardPage() {
		super("New SWTBot Test Plugin");
		setTitle("SWTBot plugin project");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		Label label;
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		label.setText("Plug-in &Name:");

		// name, id, version, providers

		pluginName = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		pluginName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pluginName.setMessage("SWTBot Test Plugin");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		label.setText("&Plug-in id:");

		pluginId = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		pluginId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pluginId.setMessage("com.example.rcp.uitest");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		label.setText("&Version:");

		pluginVersion = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		pluginVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pluginVersion.setText("1.0.0.qualifier");
		pluginVersion.setMessage("1.0.0.qualifier");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		label.setText("&Provider:");

		pluginProvider = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		pluginProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pluginProvider.setMessage("Your Company");

		productAndApplication(composite);
		hookListeners();
		setControl(composite);
	}

	private void hookListeners() {
		applicationIdButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				productId.setEnabled(false);
				applicationId.setEnabled(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		productIdButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				applicationId.setEnabled(false);
				productId.setEnabled(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				ProjectSettingValidator validator = new ProjectSettingValidator(pluginId.getText(), pluginName.getText(), pluginVersion.getText(), getProjects(), NewPluginProjectWizardPage.this);
				validator.validate();
			}
		};

		pluginId.addModifyListener(listener);
		pluginName.addModifyListener(listener);
		pluginVersion.addModifyListener(listener);
		pluginProvider.addModifyListener(listener);
	}

	private List<String> getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		ArrayList<String> result = new ArrayList<String>();

		for (IProject project : projects) {
			result.add(project.getName());
		}
		return result;
	}


	private void productAndApplication(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 2;
		group.setLayoutData(layoutData);
		group.setLayout(new GridLayout(2, false));
		group.setText("Program to test");

		productIdButton = new Button(group, SWT.RADIO);
		productIdButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		productIdButton.setText("&Product id:");
		productIdButton.setSelection(true);

		productId = new Combo(group, SWT.DROP_DOWN);
		productId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		productId.setItems(TargetPlatform.getProducts());
		productId.setText(TargetPlatform.getDefaultProduct());

		applicationIdButton = new Button(group, SWT.RADIO);
		applicationIdButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		applicationIdButton.setText("&Application id:");

		applicationId = new Combo(group, SWT.READ_ONLY | SWT.SINGLE);
		applicationId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applicationId.setItems(TargetPlatform.getApplications());
		applicationId.setText(TargetPlatform.getDefaultApplication());
		applicationId.setEnabled(false);
	}

	public String pluginName() {
		return pluginName.getText();
	}

	public String pluginId() {
		return pluginId.getText();
	}

	public String pluginVersion() {
		return pluginVersion.getText();
	}

	public String pluginProvider() {
		return pluginProvider.getText();
	}

}
