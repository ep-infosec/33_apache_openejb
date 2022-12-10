/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.openejb.helper.annotation.actions;

import org.apache.openejb.helper.annotation.Activator;
import org.apache.openejb.helper.annotation.wizards.EJBMigrationRefactoring;
import org.apache.openejb.helper.annotation.wizards.EJBMigrationWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class GenerateAnnotationsAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	protected IResource resource;

	/**
	 * Constructor for Action1.
	 */
	public GenerateAnnotationsAction() {
		super();
		resource = null;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		EJBMigrationRefactoring refactoring = new EJBMigrationRefactoring(ResourcesPlugin.getWorkspace().getRoot());
		if (resource instanceof IProject || resource instanceof IFile) {
			refactoring.setProject(resource.getProject());			
		}
		
		if (resource instanceof IFile) {
			refactoring.setEjbJarXmlFile(resource.getProjectRelativePath().toString());
		}
		
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(new EJBMigrationWizard(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE));
		try {
			op.run(Activator.getWorkbenchWindow().getShell(), Messages.getString("org.apache.openejb.helper.annotation.refactoringWizardDialogTitle")); //$NON-NLS-1$
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			
			if (firstElement instanceof IResource) {
				resource = ((IResource) firstElement);
			}
			
			if (firstElement instanceof IProject) {
				resource = (IProject) firstElement;
			}
			
			if (firstElement instanceof IJavaProject) {
				resource =  ((IJavaProject)firstElement).getResource();
			}
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

}
