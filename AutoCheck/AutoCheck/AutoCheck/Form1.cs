using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace AutoCheck
{
    public partial class Form1 : Form
    {
        private String realLogData;
        private int progressValue;
        readonly object stateLock = new object();
        public Form1()
        {
            InitializeComponent();
        }

        private List<String> DirSearch(string sDir)
        {
            List<String> files = new List<String>();
            try
            {
                foreach (string f in Directory.GetFiles(sDir))
                {
                    files.Add(f);
                }
                foreach (string d in Directory.GetDirectories(sDir))
                {
                    files.AddRange(DirSearch(d));
                }
            }
            catch (System.Exception excpt)
            {
                MessageBox.Show(excpt.Message);
            }

            return files;
        }

        private void checkBtn_Click(object sender, EventArgs e)
        {
            Thread checkTest = new Thread(() => doWork(folderTB.Text, ratioLeft.Text, ratioRight.Text, sizeLeft.Text, sizeRight.Text, ratioBtn.Checked, sizeBtn.Checked));
            checkTest.Start();
        }

        private void updateUI()
        {
            String tempLog = "";
            int tempValue = 0;
            lock (stateLock)
            {
                tempLog = realLogData;
                tempValue = progressValue;
            }
            progressBar.Value = progressValue;
            logTB.Text = tempLog;
        }

        private void doWork(String folderPath, String ratioLeftText, String ratioRightText, String sizeLeftText, String sizeRightText, bool ratioBtnChecked, bool sizeBtnChecked)
        {
            MethodInvoker methodInvoker = new MethodInvoker(updateUI);

            String logData = "";
            if (folderPath.Length == 0)
            {
                logData = "Chua nhap folder";
            }
            else
            {
                bool error = false;
                bool isRatio = false;
                int left = 0;
                int right = 0;
                if (ratioBtnChecked)
                {
                    if (ratioLeftText.Length == 0 || ratioRightText.Length == 0)
                    {
                        error = true;
                        logData += "\r\n Hay dien so vao o ratio";
                    }

                    if (System.Text.RegularExpressions.Regex.IsMatch(ratioLeftText, "[^0-9]"))
                    {
                        logData += "\r\n Chi duoc nhap so vao o ratio";
                        error = true;
                    }
                    if (System.Text.RegularExpressions.Regex.IsMatch(ratioRightText, "[^0-9]"))
                    {
                        logData += "\r\n Chi duoc nhap so vao o ratio";
                        error = true;
                    }

                    if (!error)
                    {
                        left = Int32.Parse(ratioLeftText);
                        right = Int32.Parse(ratioRightText);
                        isRatio = true;
                    }
                }
                if (sizeBtnChecked)
                {

                    if (sizeLeftText.Length == 0 || sizeRightText.Length == 0)
                    {
                        error = true;
                        logData += "\r\n Hay dien so vao o width / height";
                    }

                    if (System.Text.RegularExpressions.Regex.IsMatch(sizeLeftText, "[^0-9]"))
                    {
                        logData += "\r\n Chi duoc nhap so vao o width";
                        error = true;
                    }
                    if (System.Text.RegularExpressions.Regex.IsMatch(sizeRightText, "[^0-9]"))
                    {
                        logData += "\r\n Chi duoc nhap so vao o height";
                        error = true;
                    }

                    if (!error)
                    {
                        left = Int32.Parse(sizeLeftText);
                        right = Int32.Parse(sizeRightText);
                        isRatio = false;
                    }
                }

                // xu ly o day
                if (!error)
                {
                    // lay danh sach file
                    List<String> listFile = DirSearch(folderPath);
                    for (int i = 0; i < listFile.Count; i++)
                    {
                        String file = listFile.ElementAt(i);
                        // kiem tra width / height
                        String message = "";
                        try
                        {
                            System.Drawing.Image img = System.Drawing.Image.FromFile(file);

                            if (isRatio)
                            {
                                if (img.Width * right - img.Height * left != 0)
                                {
                                    message += " Width: " + img.Width + " Height: " + img.Height + " Ratio " + left + "/" + right;
                                }
                            }
                            else
                            {
                                if (img.Width != left)
                                {
                                    message += " Width: " + img.Width + " Need: " + left;
                                }
                                if (img.Height != right)
                                {
                                    message += " Height: " + img.Height + " Need: " + right;
                                }
                            }
                        }
                        catch
                        {
                            message = "Khong doc duoc";
                        }

                        if (message.Length != 0)
                        {
                            logData = logData + " \r\n" + file + " : " + message;
                        }

                        lock (stateLock)
                        {
                            realLogData = "";
                            progressValue = i * 100 / listFile.Count;
                        }
                        Invoke(methodInvoker);
                        //progressBar.Value = i * 100 / listFile.Count;
                    }
                    //progressBar.Value = 100;
                }
            }

            lock (stateLock)
            {
                realLogData = logData;
                progressValue = 100;
            }
            Invoke(methodInvoker);
        }
    }
}
