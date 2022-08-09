from locust import TaskSet, task, between
from locust.contrib.fasthttp import FastHttpLocust


class MyTaskSet(TaskSet):
    @task
    def index(self):
        self.client.get("/")


class MyLocust(FastHttpLocust):
    task_set = MyTaskSet
    wait_time = between(0, 0)
