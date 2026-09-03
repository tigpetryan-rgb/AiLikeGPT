import unittest

from ailikegpt.plugins.builtin.calculator import CalculatorTool


class CalculatorToolTests(unittest.TestCase):
    def setUp(self) -> None:
        self.tool = CalculatorTool()

    def test_basic_arithmetic(self) -> None:
        self.assertEqual(
            self.tool.run({"expression": "(12 + 3) * 4 / 2"}),
            "30",
        )

    def test_rejects_code_execution(self) -> None:
        with self.assertRaises(ValueError):
            self.tool.run({"expression": "__import__('os').system('echo nope')"})


if __name__ == "__main__":
    unittest.main()
