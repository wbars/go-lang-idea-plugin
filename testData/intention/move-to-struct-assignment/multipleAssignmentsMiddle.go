package main

type S struct {
	foo string
}

func main() {
	var b, a
	s := S{}
	a, <caret>s.foo, b = "a", "bar", "c"
	print(s.foo)
	print(b)
	print(a)
}