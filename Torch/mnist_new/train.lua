require 'clidebugger.debugger'

require 'torch'
require 'nn'
require 'nnx'
require 'optim'
require 'image'
require 'pl'
require 'paths'

local mnist = require 'mnist'

local trainData = mnist.traindataset()
local testData = mnist.testdataset()
print(trainData)
print(testData)
----------------------------------------------------------------------
-- define model to train
-- on the 10-class classification problem
--
classes = {'0','1','2','3','4','5','6','7','8','9'}

-- geometry: width and height of input images
geometry = {28,28}

model = nn.Sequential()
------------------------------------------------------------
-- convolutional network 
------------------------------------------------------------
-- stage 1 : mean suppresion -> filter bank -> squashing -> max pooling
-- model:	add(nn.SpatialConvolutionMM(1, 28, 5, 5))
-- model:add(nn.Tanh())
-- model:add(nn.SpatialMaxPooling(3, 3, 3, 3))
-- -- stage 2 : mean suppresion -> filter bank -> squashing -> max pooling
-- model:add(nn.SpatialConvolutionMM(28, 56, 5, 5))
-- model:add(nn.Tanh())
-- model:add(nn.SpatialMaxPooling(2, 2, 2, 2))
-- -- stage 3 : standard 2-layer MLP:
-- model:add(nn.Reshape(56*2*2))
-- model:add(nn.Linear(56*2*2, 200))
-- model:add(nn.Tanh())
-- model:add(nn.Linear(200, #classes))

----------------------------------------------------------------------
-- loss function: negative log-likelihood
model:add(nn.LogSoftMax())
criterion = nn.ClassNLLCriterion()

print(model)

dataset={};
function dataset:size() return tonumber(trainData.size) end -- 100 examples

for i=1, dataset:size() do 
  local input = torch.DoubleTensor(28,28):copy(trainData.data[i])     -- normally distributed example in 2d
  local output = tonumber(trainData.label[i]);
  dataset[i] = {input, output}
end

trainer = nn.StochasticGradient(model, criterion)
trainer.learningRate = 0.001
trainer.maxIteration = 5 -- just do 5 epochs of training.

pause('trainer')
trainer:train(dataset)

print('done')
print(trainData)
print(testData)




