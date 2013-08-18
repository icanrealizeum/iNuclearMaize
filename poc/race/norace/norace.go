//this code is from here: https://groups.google.com/d/msg/golang-nuts/NDQBTV9lH7s/6fXERiPoYAkJ
//to run, choose one of:
//go run -race norace.go
//go build && norace.exe

package main

import (
	"fmt"
	"time"

	"math/rand"
)

func main() {
	x := fanIn(boring("A"), boring("B"), boring("C"), boring("D"))
	for i := 0; i < 20; i++ {
		fmt.Println(<-x)
	}
	fmt.Println("You're both boring; I'm leaving.")
}

func fanIn(inputs ...<-chan string) <-chan string {
	c := make(chan string)
	for _, input := range inputs {
		go func(input <-chan string) {
			for {
				c <- <-input
			}
		}(input)
	}
	return c
}

func boring(msg string) <-chan string {
	c := make(chan string)
	go func() { // We launch the goroutine from inside the function.
		for i := 0; ; i++ {
			c <- fmt.Sprintf("%s %d", msg, i)
			time.Sleep(time.Duration(rand.Intn(1e3)) * time.Millisecond)
		}
	}()
	return c // Return the channel to the caller.
}
