package org.eclipse.export.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.export.Activator;
import org.eclipse.export.util.DirList;
import org.eclipse.export.util.FileUtil;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * @addtime 2015-07-03 13:21:30
 * @author gl
 */
@SuppressWarnings("restriction")
public class NewAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public NewAction() {

	}

	public static boolean isNotEmpty(String str) {
		return (str != null && str.length() != 0);
	}

	public void run(IAction action) {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		URL url = bundle.getResource("/prop.properties"); //获取配置文件
		Properties prop = new Properties();

		try {
			InputStream is = FileLocator.toFileURL(url).openStream();
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String workspace = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString();// 获取工作空间

		String exportPath = prop.getProperty("ExportPath"); //获取配置文件中的文件导出路径

		IWorkbench workbench = PlatformUI.getWorkbench();  //获取工作台
		IWorkbenchWindow win = workbench.getActiveWorkbenchWindow(); //获取工作台窗口
		
		ISelectionService selectionService = win.getSelectionService();  
		ISelection selection = selectionService.getSelection();  //获取当前选择

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			//判断当前是否选择了对象
			if (element == null) {
				MessageDialog.openWarning(window.getShell(), "Warning",
						"Please select a file or activate the edit window");
				return;
			} else if (element instanceof IJavaElement) { //导出Java文件
				IJavaElement ije = (IJavaElement) element;
				//判断选择的文件是否为java文件
				if (isNotEmpty(ije.getPath().getFileExtension())
						&& ije.getPath().getFileExtension().equals("java")) {
					String projectName = ije.getJavaProject().getElementName(); // 应用名称
					Map<String, String> xml = XMLUtil.xml(workspace,
							projectName);	//获取项目配置文件
					String filepath = ije.getPath().toOSString();
					if (isNotEmpty(filepath) && isNotEmpty(projectName)) {
						if (filepath.indexOf("\\") == 0) {
							filepath = filepath.replaceFirst("\\\\", "");
						}
						String[] str = filepath.split("\\\\");
						if (str.length > 2 && filepath.contains(projectName)) {
							// 替换两遍
							filepath = filepath.substring(filepath
									.indexOf("\\") + 1);
							filepath = filepath.substring(filepath
									.indexOf("\\"));

							String path = xml.get("output")
									+ filepath.substring(0,
											filepath.lastIndexOf("\\"));
							String dir = workspace + "\\" + projectName + "\\"
									+ path;
							String filename = filepath.substring(
									filepath.lastIndexOf("\\") + 1).replaceAll(
									".java", "");

							String[] files = DirList.getFile(dir, filename
									+ "(\\$)?([0-9])*\\.class");	//匹配内部类，匿名类以及行数多的类
							if (files == null || files.length == 0) {
								MessageDialog
										.openWarning(window.getShell(),
												"Warning",
												"Please wait a few seconds and try again");
								return;
							}
							for (int i = 0; i < files.length; i++) {
								//复制文件到指定导出位置
								FileUtil.copyFile(dir + "\\", exportPath
										+ projectName + "\\" + path + "\\",
										files[i]);
							}
							//如果编译路径下不存在当前java的class文件则提示用户文件未找到
							if (!new File(exportPath + projectName + "\\"
									+ path + "\\" + filename + ".class")
									.exists()) {
								MessageDialog.openWarning(window.getShell(),
										"Warning", "File not fount");
							} else {
								MessageDialog.openInformation(
										window.getShell(), "Success",
										"File export success");
							}
							return;
						}
					}

					MessageDialog.openWarning(window.getShell(), "Warning",
							"Do not support this type of file export");
				} else {
					MessageDialog.openWarning(window.getShell(), "Warning",
							"Do not support this type of file export");
				}
			} else if (element instanceof IResource) {  //导出其他资源文件
				IResource ir = (IResource) element;
				//判断文件类型为zhtml或jsp  此处可以自定义类型
				if (isNotEmpty(ir.getFileExtension())
						&& (ir.getFileExtension().equals("zhtml") || ir.getFileExtension().equals("jsp")|| ir.getFileExtension().equals("js")|| ir.getFileExtension().equals("css"))) {
					String newfilepath = exportPath + ir.getFullPath().toOSString();
					if(System.getProperties().getProperty("os.name").equals("Mac OS X")){
						newfilepath = newfilepath.replaceAll("//", "/");
					}
					FileUtil.copyFile(ir.getLocation().toOSString(), newfilepath);
					if (!new File(newfilepath).exists()) {
						MessageDialog.openWarning(window.getShell(), "Warning",
								"File not fount(" + ir.getLocation().toOSString() + ")TO(" + exportPath + ir.getFullPath().toOSString() + ")");
					} else {
						MessageDialog.openInformation(window.getShell(),
								"Success", "File export success");
					}
				} else {
					MessageDialog.openInformation(window.getShell(), "Success",
							"Do not support this type of file export");
				}
			} else if (element instanceof PackageFragmentRootContainer) {//其他导出类型可以自行扩展
				MessageDialog.openInformation(window.getShell(), "Success",
						"Do not support this type of file export");
			} else {//其他导出类型可以自行扩展
				MessageDialog.openWarning(window.getShell(), "Warning",
						"Do not support this type of file export");
			}
		} else {
			// 取得工作台窗体并取得当前处于活动状态的编辑框
			IWorkbenchPage page = window.getActivePage();
			IEditorPart part = page.getActiveEditor();

			String fileName = part.getTitleToolTip();
			if (isNotEmpty(fileName) && fileName.endsWith(".java")) {
				String ext = "\\";
				if (System.getProperties().getProperty("os.name").equals("Mac OS X") || fileName.indexOf(ext) == -1) {
					ext = "/";
				}
				String projectName = fileName.substring(0,
						fileName.indexOf(ext));
				String[] str = fileName.split(ext);

				Map<String, String> xml = XMLUtil.xml(workspace, projectName);

				if (str.length > 2 && fileName.contains(projectName)) {
					// 替换两遍
					fileName = fileName.substring(fileName.indexOf(ext) + 1);
					fileName = fileName.substring(fileName.indexOf(ext));

					String path = projectName + ext + xml.get("output")
							+ fileName.substring(0, fileName.lastIndexOf(ext));
					String dir = workspace + ext + path;
					String fname = fileName.substring(
							fileName.lastIndexOf(ext) + 1).replaceAll(".java",
							"");

					String[] files = DirList.getFile(dir, fname
							+ "(\\$)?([0-9])*\\.class");
					if (files == null || files.length == 0) {
						MessageDialog.openWarning(window.getShell(), "Warning",
								"Please wait a few seconds and try again");
						return;
					}
					for (int i = 0; i < files.length; i++) {
						FileUtil.copyFile(dir + ext, exportPath + path + ext,
								files[i]);
					}
					if (!new File(exportPath + path + ext + fname + ".class")
							.exists()) {
						MessageDialog.openWarning(window.getShell(), "Warning",
								"The current editor file not found(" + exportPath + path + ext + fname + ".class)");
					} else {
						MessageDialog.openInformation(window.getShell(),
								"Success",
								"Export success of the current edited file");
					}
					return;
				}
			} else if (isNotEmpty(fileName)) {
				String filepath = "";
				if(System.getProperties().getProperty("os.name").equals("Mac OS X")){
					filepath = workspace + "/" + fileName;
				}else{
					filepath = workspace + "\\" + fileName;
				}
				FileUtil.copyFile(filepath, exportPath
						+ fileName);
				if (!new File(exportPath + fileName).exists()) {
					MessageDialog.openWarning(window.getShell(), "Warning",
							"The current editor file not found(" + exportPath + fileName + ")");
				} else {
					MessageDialog.openInformation(window.getShell(), "Success",
							"Export success of the current edited file");
				}
			} else {
				MessageDialog.openWarning(window.getShell(), "Warning",
						"Please select a file or activate the edit window");
			}
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}