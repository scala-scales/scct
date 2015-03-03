package scoverage.report

import java.io.File

import scoverage._

import scala.xml.Node

/** @author Stephen Samuel */
class CoberturaXmlWriter(sourceDirectories: Seq[File], outputDir: File) {

  def this (baseDir: File, outputDir: File) {
    this(Seq(baseDir), outputDir);
  }

  // Source paths in canonical form WITH trailing file separator
  val formattedSourcePaths: Seq[String] = sourceDirectories filter ( _.isDirectory ) map ( _.getCanonicalPath + File.separator )
  
  def format(double: Double): String = "%.2f".format(double)

  def write(coverage: Coverage): Unit = {
    IOUtils.writeToFile(new File(outputDir, "cobertura.xml"),
      "<?xml version=\"1.0\"?>\n<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/coverage-04.dtd\">\n" +
        xml(coverage))
  }

  def method(method: MeasuredMethod): Node = {
    <method name={method.name}
            signature="()V"
            line-rate={format(method.statementCoverage)}
            branch-rate={format(method.branchCoverage)}>
      <lines>
        {method.statements.map(stmt =>
          <line
          number={stmt.line.toString}
          hits={stmt.count.toString}
          branch="false"/>
      )}
      </lines>
    </method>
  }

  def klass(klass: MeasuredClass): Node = {
    <class name={klass.name}
           filename={relativeSource(klass.source).replace(File.separator, "/")}
           line-rate={format(klass.statementCoverage)}
           branch-rate={format(klass.branchCoverage)}
           complexity="0">
      <methods>
        {klass.methods.map(method)}
      </methods>
      <lines>
        {klass.statements.map(stmt =>
          <line
          number={stmt.line.toString}
          hits={stmt.count.toString}
          branch="false"/>
      )}
      </lines>
    </class>
  }

  def pack(pack: MeasuredPackage): Node = {
    <package name={pack.name}
             line-rate={format(pack.statementCoverage)}
             branch-rate={format(pack.branchCoverage)}
             complexity="0">
      <classes>
        {pack.classes.map(klass)}
      </classes>
    </package>
  }

  def source(src: File): Node = {
    <source>{src.getCanonicalPath.replace(File.separator, "/")}</source>
  }

  def xml(coverage: Coverage): Node = {
    <coverage line-rate={format(coverage.statementCoverage)}
              lines-covered={coverage.statementCount.toString}
              lines-valid={coverage.invokedStatementCount.toString}
              branches-covered={coverage.branchCount.toString}
              branches-valid={coverage.invokedBranchesCount.toString}
              branch-rate={format(coverage.branchCoverage)}
              complexity="0"
              version="1.0"
              timestamp={System.currentTimeMillis.toString}>
      <sources>
        <source>--source</source>
        {sourceDirectories.filter(_.isDirectory()).map(source)}
      </sources>
      <packages>
        {coverage.packages.map(pack)}
      </packages>
    </coverage>
  }

  private def relativeSource(src: String): String = IOUtils.relativeSource(src, formattedSourcePaths)

}
