namespace AutoCheck
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.folderTB = new System.Windows.Forms.TextBox();
            this.ratioBtn = new System.Windows.Forms.RadioButton();
            this.sizeBtn = new System.Windows.Forms.RadioButton();
            this.ratioLeft = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.ratioRight = new System.Windows.Forms.TextBox();
            this.sizeRight = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.sizeLeft = new System.Windows.Forms.TextBox();
            this.checkBtn = new System.Windows.Forms.Button();
            this.logTB = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.progressBar = new System.Windows.Forms.ProgressBar();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(25, 48);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(36, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Folder";
            // 
            // folderTB
            // 
            this.folderTB.Location = new System.Drawing.Point(67, 45);
            this.folderTB.Name = "folderTB";
            this.folderTB.Size = new System.Drawing.Size(764, 20);
            this.folderTB.TabIndex = 1;
            // 
            // ratioBtn
            // 
            this.ratioBtn.AutoSize = true;
            this.ratioBtn.Checked = true;
            this.ratioBtn.Location = new System.Drawing.Point(413, 101);
            this.ratioBtn.Name = "ratioBtn";
            this.ratioBtn.Size = new System.Drawing.Size(50, 17);
            this.ratioBtn.TabIndex = 2;
            this.ratioBtn.TabStop = true;
            this.ratioBtn.Text = "Ratio";
            this.ratioBtn.UseVisualStyleBackColor = true;
            // 
            // sizeBtn
            // 
            this.sizeBtn.AutoSize = true;
            this.sizeBtn.Location = new System.Drawing.Point(413, 135);
            this.sizeBtn.Name = "sizeBtn";
            this.sizeBtn.Size = new System.Drawing.Size(95, 17);
            this.sizeBtn.TabIndex = 3;
            this.sizeBtn.Text = "Width / Height";
            this.sizeBtn.UseVisualStyleBackColor = true;
            // 
            // ratioLeft
            // 
            this.ratioLeft.Location = new System.Drawing.Point(533, 101);
            this.ratioLeft.Name = "ratioLeft";
            this.ratioLeft.Size = new System.Drawing.Size(71, 20);
            this.ratioLeft.TabIndex = 4;
            this.ratioLeft.Text = "1";
            this.ratioLeft.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(611, 105);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(12, 13);
            this.label2.TabIndex = 5;
            this.label2.Text = "/";
            // 
            // ratioRight
            // 
            this.ratioRight.Location = new System.Drawing.Point(629, 101);
            this.ratioRight.Name = "ratioRight";
            this.ratioRight.Size = new System.Drawing.Size(71, 20);
            this.ratioRight.TabIndex = 6;
            this.ratioRight.Text = "1";
            this.ratioRight.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // sizeRight
            // 
            this.sizeRight.Location = new System.Drawing.Point(629, 135);
            this.sizeRight.Name = "sizeRight";
            this.sizeRight.Size = new System.Drawing.Size(71, 20);
            this.sizeRight.TabIndex = 9;
            this.sizeRight.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(611, 139);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(12, 13);
            this.label3.TabIndex = 8;
            this.label3.Text = "/";
            // 
            // sizeLeft
            // 
            this.sizeLeft.Location = new System.Drawing.Point(533, 135);
            this.sizeLeft.Name = "sizeLeft";
            this.sizeLeft.Size = new System.Drawing.Size(71, 20);
            this.sizeLeft.TabIndex = 7;
            this.sizeLeft.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // checkBtn
            // 
            this.checkBtn.Location = new System.Drawing.Point(733, 101);
            this.checkBtn.Name = "checkBtn";
            this.checkBtn.Size = new System.Drawing.Size(98, 54);
            this.checkBtn.TabIndex = 10;
            this.checkBtn.Text = "Check";
            this.checkBtn.UseVisualStyleBackColor = true;
            this.checkBtn.Click += new System.EventHandler(this.checkBtn_Click);
            // 
            // logTB
            // 
            this.logTB.Location = new System.Drawing.Point(67, 183);
            this.logTB.Multiline = true;
            this.logTB.Name = "logTB";
            this.logTB.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.logTB.Size = new System.Drawing.Size(764, 249);
            this.logTB.TabIndex = 11;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(25, 183);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(25, 13);
            this.label4.TabIndex = 12;
            this.label4.Text = "Log";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(546, 85);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(44, 13);
            this.label5.TabIndex = 13;
            this.label5.Text = "WIDTH";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(643, 85);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(48, 13);
            this.label6.TabIndex = 14;
            this.label6.Text = "HEIGHT";
            // 
            // progressBar
            // 
            this.progressBar.Location = new System.Drawing.Point(67, 161);
            this.progressBar.Name = "progressBar";
            this.progressBar.Size = new System.Drawing.Size(764, 16);
            this.progressBar.TabIndex = 15;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(902, 466);
            this.Controls.Add(this.progressBar);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.logTB);
            this.Controls.Add(this.checkBtn);
            this.Controls.Add(this.sizeRight);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.sizeLeft);
            this.Controls.Add(this.ratioRight);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.ratioLeft);
            this.Controls.Add(this.sizeBtn);
            this.Controls.Add(this.ratioBtn);
            this.Controls.Add(this.folderTB);
            this.Controls.Add(this.label1);
            this.Name = "Form1";
            this.Text = "AutoCheck";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox folderTB;
        private System.Windows.Forms.RadioButton ratioBtn;
        private System.Windows.Forms.RadioButton sizeBtn;
        private System.Windows.Forms.TextBox ratioLeft;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox ratioRight;
        private System.Windows.Forms.TextBox sizeRight;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox sizeLeft;
        private System.Windows.Forms.Button checkBtn;
        private System.Windows.Forms.TextBox logTB;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.ProgressBar progressBar;
    }
}

