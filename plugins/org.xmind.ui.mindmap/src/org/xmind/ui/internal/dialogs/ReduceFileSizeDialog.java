package org.xmind.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.editor.MindMapEditor;

public class ReduceFileSizeDialog extends Dialog {
    private IEditorPart editor;
    private Button editingHistoryCheckbox;
    private Button previewImageCheckbox;

    public ReduceFileSizeDialog(IEditorPart editor) {
        super(editor.getSite().getShell());
        this.editor = editor;
    }

    public void create() {
        super.create();
//        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Label label = new Label(composite, SWT.WRAP);
        label.setText(DialogMessages.ReduceFileSize_Advise_text);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 380;
        gridData.heightHint = SWT.DEFAULT;
        label.setLayoutData(gridData);

        editingHistoryCheckbox = new Button(composite, SWT.CHECK);
        editingHistoryCheckbox.setSelection(true);
        editingHistoryCheckbox
                .setText(DialogMessages.DeleteEditingHistory_text);
        previewImageCheckbox = new Button(composite, SWT.CHECK);
        previewImageCheckbox.setText(DialogMessages.DeletePreviewImage_text);

        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(
                        editingHistoryCheckbox.getSelection()
                                || previewImageCheckbox.getSelection());
            }
        };
        editingHistoryCheckbox.addSelectionListener(listener);
        previewImageCheckbox.addSelectionListener(listener);

        return composite;
    }

    @Override
    protected void okPressed() {
        boolean deleteEditingHistory = editingHistoryCheckbox.getSelection();
        boolean deletePreviewImage = previewImageCheckbox.getSelection();

        if (!deleteEditingHistory && !deletePreviewImage)
            return;

        IWorkbook workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);

        if (deleteEditingHistory) {
            for (ISheet sheet : workbook.getSheets()) {
                IRevisionManager rm = workbook.getRevisionRepository()
                        .getRevisionManager(sheet.getId(), IRevision.SHEET);
                List<IRevision> revisions = new ArrayList<IRevision>(
                        rm.getRevisions());
                for (IRevision r : revisions) {
                    rm.removeRevision(r);
                }
            }
        }
        if (deletePreviewImage) {
            ((MindMapEditor) editor).skipNextPreviewImage();
        }

        NullProgressMonitor progress = new NullProgressMonitor();
        editor.doSave(progress);
        if (progress.isCanceled()) {
            getShell().setActive();
            return;
        }

        super.okPressed();

    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.ReduceFileSize_text);
    }

}
