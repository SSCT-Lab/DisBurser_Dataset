#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require 'hbase'
require 'hbase/table'
require 'shell'

##
# Tests whether all registered commands have a help and command method

class ShellCommandsTest < Test::Unit::TestCase
  Shell.commands.each do |name, klass|
    define_test "#{name} command class #{klass} should respond to help" do
      assert_respond_to(klass.new(nil), :help)
    end

    define_test "#{name} command class #{klass} should respond to :command" do
      assert_respond_to(klass.new(nil), :command)
    end
  end
end

##
# Tests commands from the point of view of the shell to validate
# that the error messages returned to the user are correct

class ShellCloneSnapshotTest < Test::Unit::TestCase
  include Hbase::TestHelpers

  def setup
    setup_hbase
    @shell.interactive = false
    # Create test table
    @test_name = 'hbase_shell_tests_table'
    drop_test_table(@test_name)
    create_test_table(@test_name)
    # Test snapshot name
    @create_test_snapshot = 'hbase_shell_tests_snapshot'
    drop_test_snapshot
  end

  def teardown
    drop_test_table(@test_name)
    drop_test_snapshot
    shutdown
  end

  define_test 'Clone snapshot with table that already exists' do
    existing_table = 'existing_table'
    create_test_table(existing_table)
    admin.snapshot(@test_name, @create_test_snapshot)
    error = assert_raise(RuntimeError) do
      @shell.command(:clone_snapshot, @create_test_snapshot, existing_table)
    end
    assert_match(/Table already exists: existing_table!/, error.message)
  end

  define_test 'Clone snapshot with unknown namespace' do
    clone_table = 'does_not_exist:test_clone_snapshot_table'
    admin.snapshot(@test_name, @create_test_snapshot)
    error = assert_raise(RuntimeError) do
      @shell.command(:clone_snapshot, @create_test_snapshot, clone_table)
    end
    assert_match(/Unknown namespace: does_not_exist!/, error.message)
  end
end
