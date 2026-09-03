from ailikegpt.plugins.builtin.calculator import CalculatorTool


def test_basic_arithmetic() -> None:
    tool = CalculatorTool()
    assert tool.run({"expression": "(12 + 3) * 4 / 2"}) == "30"


def test_rejects_code_execution() -> None:
    tool = CalculatorTool()
    try:
        tool.run({"expression": "__import__('os').system('echo nope')"})
    except ValueError:
        pass
    else:
        raise AssertionError("calculator accepted a non-arithmetic expression")
