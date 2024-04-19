
import torch.nn as nn
class MLP(nn.Module):
    def __init__(self, input_size, output_size):
        super(MLP, self).__init__()
        self.fc = []
        layers = [32,256,64]
        self.fc1 = nn.Linear(input_size, layers[0])
        self.activate = nn.Tanh()
        for i in range(len(layers)-1):
            self.fc.append(nn.Linear(layers[i], layers[i+1]))
            if i !=len(layers)-1:
                self.fc.append(self.activate)
            self.all_layers = nn.Sequential(*self.fc)
        self.fc2 = nn.Linear(layers[-1], output_size)

    def forward(self, x):
        x = x.reshape(x.shape[0], -1)
        out = self.fc1(x)
        out = self.activate(out)
        out = self.all_layers(out)
        out = self.fc2(out)
        return out
