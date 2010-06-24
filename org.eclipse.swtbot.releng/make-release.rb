#!/usr/bin/env ruby

require 'rubygems'
require 'rake'
require 'fileutils'
require 'optparse'
require 'open-uri'

class Release
  def self.run(args)
    @now = Time.now.strftime('%b %d, %Y')
    puts "Fetching revision available on the download site..."
    uri = URI.parse("http://download.eclipse.org/technology/swtbot/ganymede/dev-build/RELEASE_NOTES.txt")
    puts "Reading #{uri}"

    @available_revision = open(uri).readlines.grep(/Revision: (.){40}/).first.gsub(/Revision: /, '').strip

    @current_head = `git log --pretty='%H' -1`.strip
    @current_head_svn = `git log -1`.gsub(/.*git-svn-id:.*@(\d+).*/m, '\1')

    puts "Revision on the download site: #{@available_revision}"
    puts "Generating revision log since  #{@available_revision} to HEAD(#{@current_head})"

    @revision_log = `git log --pretty='%h - by %cn on %cd%n%s%n%b%n' --date=short #{@available_revision}..#{@current_head}`
    
    @revision_log.gsub!(/.*git-svn-id:.*@(\d+).*/, '  svn-revision: \1')
    @revision_log = @revision_log.gsub(/\t/,"     ").gsub(/.{1,72}(?:\s|\Z)/){($& + 5.chr).gsub(/\n\005/,"\n  ").gsub(/\005/,"\n  ")}
    
    @revision_log += "\n\n"
    @revision_log += open(uri).read

    FileUtils.rm_rf('to-upload')
    FileUtils.rm_rf('target')

    build_swtbot(34, 'ganymede')
    build_swtbot(35, 'galileo')
    build_swtbot(36, 'helios')
  end

  def self.release_notes(dir)
    File.open("#{dir}/RELEASE_NOTES.txt", 'w') do |f|
      title = "RELEASE NOTES v#{@current_head_svn} (#{@now})"
      f.puts(title)
      f.puts("=" * title.length)
      f.puts("")
      rev = "Revision: #{@current_head}"
      f.puts("=" * rev.length)
      f.puts(rev)
      f.puts("=" * rev.length)
      f.puts("")
      f.puts(@revision_log)
    end
  end

  #version=34, code_name=ganymede
  #version=35, code_name=galileo
  #version=36, code_name=helios
  def self.build_swtbot(version, code_name)

    FileUtils.rm_rf("to-upload/#{code_name}")
    FileUtils.mkdir_p("to-upload/#{code_name}/dev-build")
    release_notes("to-upload/#{code_name}/dev-build")

    sh("ant materialize-workspace -Declipse.version=#{version} -Dhas.archives=true")
    sh("ant cruise -Declipse.version=#{version} -Dhas.archives=true")
    FileUtils.rm_rf("to-upload/#{code_name}")
    FileUtils.mkdir_p("to-upload/#{code_name}")
    FileUtils.mv("artifacts/to-upload", "to-upload/#{code_name}/dev-build")
    release_notes("to-upload/#{code_name}/dev-build")
  end
end

Release.run(ARGV)
