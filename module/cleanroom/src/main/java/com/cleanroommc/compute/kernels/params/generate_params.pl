@filenames = (
	"Double",
	"Byte",
	"Short",
	"Int",
	"Long"#,
#	"Pointer"
);

sub template
{
	return ("package com.cleanroommc.cleanroom.compute.kernels.params;\n",
		"\n",
		"import com.cleanroommc.cleanroom.compute.errors.KernelError;\n",
		"import org.lwjgl.opencl.CL10;\n",
		"\n",
		"import java.nio.", $_[0], "Buffer;\n",
		"\n",
		"import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;\n",
		"\n",
		"record Buffer", $_[0], "Parameter(", $_[0], "Buffer value) implements KernelParameter {\n",
		"\t\@Override\n",
		"\tpublic void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {\n",
		"\t\thandleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);\n",
		"\t}\n",
		"}\n");
};

foreach $fname (@filenames) {
	my($read_data);
	my $tmp = "Buffer" . $fname . "Parameter.java";
	open(DATA, '>', $tmp) or die "Error in reading the file " . $tmp;
	print DATA template($fname);
	close(DATA);
}